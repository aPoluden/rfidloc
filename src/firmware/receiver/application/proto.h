#ifndef __PROTO_H__
#define __PROTO_H__

#include "openbeacon.h"

#define FIFO_DEPTH 256

typedef struct {
    
    u_int16_t tag_oid;
    u_int8_t tag_strength;
    u_int8_t packet_count;
    
} __attribute__((packed)) TBeaconSort;

extern TBeaconEnvelope g_Beacon;

extern void vInitProtocolLayer(void);
extern int PtDebugLevel(int DebugLevel);
extern int PtSetFifoLifetimeSeconds(int Seconds);
extern int PtGetFifoLifetimeSeconds(void);
extern void tx_tag_command(unsigned int tag_id, unsigned int tag_id_new);
extern void wifi_tx_reader_command(unsigned int reader_id, unsigned char opcode, unsigned int data0, unsigned int data1);

#endif/*__PROTO_H__*/
