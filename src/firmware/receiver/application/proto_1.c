#include <FreeRTOS.h>
#include <semphr.h>
#include <task.h>
#include <string.h>
#include <board.h>
#include <beacontypes.h>
#include <sort.h>
#include "led.h"
#include "xxtea.h"
#include "proto_1.h"
#include "env.h"
#include "debug_printf.h"
#include "nRF24L01/nRF_HW.h"
#include "nRF24L01/nRF_CMD.h"
#include "nRF24L01/nRF_API.h"

#define TAG_ID_MASK (0x0000FFFFL)
#define UPDATE_INTERVAL_MS (portTICK_RATE_MS*250)
#define ANNOUNCEMENT_TX_POWER 3
#define SORT_PRINT_DEPTH 8

#define BCFLAGS_VALIDENTRY 0x01

typedef struct {
    
    u_int8_t bcflags;
    u_int8_t tag_strength;
    u_int16_t reserved;
    u_int16_t tag_oid;
    u_int32_t arrival_time;
    
} __attribute__((packed)) TBeaconCache;

// set broadcast mac
const unsigned char broadcast_mac[NRF_MAX_MAC_SIZE] = { 1, 2, 3, 2, 1 };

TBeaconCache g_BeaconCache[FIFO_DEPTH];
int g_BeaconSortSize;
u_int16_t last_reader_id;
TBeaconSort g_BeaconSort[FIFO_DEPTH];
TBeaconSort g_BeaconSortPrint[SORT_PRINT_DEPTH];
unsigned int g_BeaconSortTmp[FIFO_DEPTH];
unsigned int g_BeaconFifoLifeTime;
volatile u_int32_t g_BeaconCacheHead;
TBeaconEnvelope g_Beacon, g_BeaconTx;
xSemaphoreHandle PtSemaphore;
TBeaconReaderCommand reader_command;
int g_debuglevel;


/**********************************************************************/
#define SHUFFLE(a,b)    tmp=g_Beacon.byte[a];\
                        g_Beacon.byte[a]=g_Beacon.byte[b];\
                        g_Beacon.byte[b]=tmp;

void RAMFUNC
shuffle_tx_byteorder(void) {
    unsigned char tmp;

    SHUFFLE(0 + 0, 3 + 0);
    SHUFFLE(1 + 0, 2 + 0);
    SHUFFLE(0 + 4, 3 + 4);
    SHUFFLE(1 + 4, 2 + 4);
    SHUFFLE(0 + 8, 3 + 8);
    SHUFFLE(1 + 8, 2 + 8);
    SHUFFLE(0 + 12, 3 + 12);
    SHUFFLE(1 + 12, 2 + 12);
    
}

static inline s_int8_t
PtInitNRF(void) {
    
    if (!nRFAPI_Init(DEFAULT_DEV, CONFIG_TRACKER_CHANNEL, broadcast_mac, sizeof (broadcast_mac), 0)) {
        return 0;
    }

    nRFAPI_SetPipeSizeRX(DEFAULT_DEV, 0, 16);
    nRFAPI_SetTxPower(DEFAULT_DEV, ANNOUNCEMENT_TX_POWER);
    nRFAPI_SetRxMode(DEFAULT_DEV, 1);

    nRFCMD_CE(DEFAULT_DEV, 1);

    return 1;
}

static inline unsigned short
swapshort (unsigned short src) {
    return (src >> 8) | (src << 8);
}

static inline unsigned long
swaplong (unsigned long src) {
    return (src >> 24) | (src << 24) | ((src >> 8) & 0x0000FF00) | ((src << 8) & 0x00FF0000);
}

/**********************************************************************/

void
vnRFtaskRxTx(void *parameter) {
    
    TBeaconCache *pcache;
    u_int16_t crc, oid;
    u_int8_t strength, t;
    portTickType LastUpdateTicks, Ticks;
    (void) parameter;

    if (!PtInitNRF()) {
        return;
    }

    LastUpdateTicks = xTaskGetTickCount();

    for (;;) {
        
        if (nRFCMD_WaitRx(10)) {
            
            vLedSetRed(1);

            do {
                
                // read packet from nRF chip
                nRFCMD_RegReadBuf(DEFAULT_DEV, RD_RX_PLOAD, g_Beacon.byte, sizeof (g_Beacon));

                // adjust byte order and decode
                shuffle_tx_byteorder();
                xxtea_decode();
                shuffle_tx_byteorder();

                // verify the CRC checksum
                crc = env_crc16(g_Beacon.byte, sizeof (g_Beacon) - sizeof (u_int16_t));
                if (swapshort(g_Beacon.pkt.crc) == crc) {
                    
                    oid = swapshort(g_Beacon.pkt.oid);

                    switch (g_Beacon.pkt.proto) {

                        case RFBPROTO_READER_ANNOUNCE:
                            strength = g_Beacon.pkt.p.reader_announce.strength;
                            break;

                        case RFBPROTO_BEACONTRACKER:
                            strength = g_Beacon.pkt.p.tracker.strength;
                            if (g_debuglevel) {
                                debug_printf(" R: %04i={%i,0x%08X}\n", (int) oid, (int) strength, swaplong(g_Beacon.pkt.p.tracker.seq));
                            }
                            break;

                        case RFBPROTO_PROXREPORT:
                        case RFBPROTO_PROXREPORT_EXT:
                            strength = 3;

                            if (g_debuglevel) {
                                debug_printf(" P: %04i={%i,0x%04X}\n", (int) oid, (int) strength, (int) swapshort(g_Beacon.pkt.p.prox.seq));
                                for (t = 0; t < PROX_MAX; t++) {
                                    crc = (swapshort(g_Beacon.pkt.p.prox.oid_prox[t]));
                                    if (crc) {
                                        debug_printf("PX: %04i={%04i,%i,%i}\n", (int) oid, (int) ((crc >> 0) & 0x7FF), (int) ((crc >> 14) & 0x3), (int) ((crc >> 11) & 0x7));
                                    }
                                }
                            }
                            break;

                        default:
                            strength = 0xFF;
                            if (g_debuglevel) {
                                debug_printf("Unknown Protocol: %i\n", (int) g_Beacon.pkt.proto);
                            }
                    }

                    if (strength < 0xFF) {
                        
                        pcache = &g_BeaconCache[g_BeaconCacheHead];
                        pcache->bcflags = 0;
                        pcache->arrival_time = xTaskGetTickCount();
                        pcache->tag_oid = oid;
                        pcache->tag_strength = strength;
                        pcache->bcflags = BCFLAGS_VALIDENTRY;

                        g_BeaconCacheHead++;
                        if (g_BeaconCacheHead >= FIFO_DEPTH) {
                            g_BeaconCacheHead = 0;
                        }
                    }
                }
                
            } while ((nRFAPI_GetFifoStatus(DEFAULT_DEV) & FIFO_RX_EMPTY) == 0);

            vLedSetRed(0);
        }
        
        nRFAPI_ClearIRQ(DEFAULT_DEV, MASK_IRQ_FLAGS);

        if (reader_command.opcode) {
            
            vLedSetGreen(1);
            bzero(&g_Beacon, sizeof (g_Beacon));
            g_Beacon.pkt.oid = swapshort(last_reader_id);
            g_Beacon.pkt.proto = RFBPROTO_READER_COMMAND;
            g_Beacon.pkt.p.reader_command = reader_command;
            wifi_tx(3, CONFIG_TRACKER_CHANNEL);
            reader_command.opcode = 0;
            vLedSetGreen(0);
            
        }

        if (g_BeaconTx.pkt.proto) {
            
            memcpy(&g_Beacon, &g_BeaconTx, sizeof (g_Beacon));
            g_BeaconTx.pkt.p.tracker.seq++;
            g_Beacon.pkt.p.tracker.seq = swaplong(g_BeaconTx.pkt.p.tracker.seq);
            wifi_tx(g_Beacon.pkt.p.tracker.strength, CONFIG_PROX_CHANNEL);
            memset(&g_Beacon, 0, sizeof (g_Beacon));
            
        }

        // update regularly
        if (((Ticks = xTaskGetTickCount()) - LastUpdateTicks) > UPDATE_INTERVAL_MS) {
            LastUpdateTicks = Ticks;
            vnRFUpdateRating();
        }
        
    }
}

void
vnRFtaskRating(void *parameter) {
    
    int count, i;
    (void) parameter;

    for (;;) {
        
        if (g_debuglevel) {
            debug_printf("DIST:");
        }

        count = vnRFCopyRating(g_BeaconSortPrint, SORT_PRINT_DEPTH);
        
        if (g_debuglevel) {
            for (i = 0; i < count; i++) {
                debug_printf(" %i,%04i", (int) g_BeaconSortPrint[i].tag_strength, (int) g_BeaconSortPrint[i].tag_oid);
            }
        }

        if (g_debuglevel) {
            debug_printf("\n");
        }
        
        vTaskDelay(portTICK_RATE_MS * 950);
    }
}

void
vInitProtocolLayer(void) {
    
    PtSetFifoLifetimeSeconds(20);
    vLedSetGreen(1);

    g_debuglevel = 1;
    g_BeaconCacheHead = 0;
    memset(&g_Beacon, 0, sizeof (g_Beacon));
    memset(&g_BeaconTx, 0, sizeof (g_BeaconTx));

    PtSemaphore = NULL;
    vSemaphoreCreateBinary(PtSemaphore);

    xTaskCreate(vnRFtaskRxTx, (signed portCHAR *) "nRF_RxTx", TASK_NRF_STACK, NULL, TASK_NRF_PRIORITY, NULL);
    xTaskCreate(vnRFtaskRating, (signed portCHAR *) "nRF_Rating", 128, NULL, TASK_NRF_PRIORITY, NULL);
    
}
