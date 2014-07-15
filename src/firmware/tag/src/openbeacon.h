#ifndef __OPENBEACON_H__
#define __OPENBEACON_H__

#define CONFIG_TRACKER_CHANNEL 81
#define CONFIG_PROX_CHANNEL 76

#define XXTEA_BLOCK_COUNT 4

#define RFBPROTO_READER_COMMAND 23
#define RFBPROTO_BEACONTRACKER  24
#define RFBPROTO_PROXTRACKER    42
#define RFBPROTO_PROXREPORT     69
#define RFBPROTO_PROXREPORT_EXT 70

#define PROX_MAX 4
#define PROX_TAG_ID_BITS 12
#define PROX_TAG_COUNT_BITS 2
#define PROX_TAG_STRENGTH_BITS 2
#define PROX_TAG_ID_MASK ((1<<PROX_TAG_ID_BITS)-1)
#define PROX_TAG_COUNT_MASK ((1<<PROX_TAG_COUNT_BITS)-1)
#define PROX_TAG_STRENGTH_MASK ((1<<PROX_TAG_STRENGTH_BITS)-1)

#define RFBFLAGS_ACK		0x01
#define RFBFLAGS_SENSOR		0x02
#define RFBFLAGS_INFECTED	0x04
#define RFBFLAGS_OID_WRITE	0x08
#define RFBFLAGS_OID_UPDATED	0x10
#define RFBFLAGS_WRAPPED_SEQ	0x20

#define OID_PERSON			0x0400
#define OID_HEALER			0x0200
#define OID_PERSON_MIN      1100

typedef struct {
    
    uint8_t proto;
    uint16_t oid;
    uint8_t flags;
    
} TBeaconHeader;

typedef struct {
    
    TBeaconHeader hdr;
    uint8_t strength;
    uint16_t oid_last_seen;
    uint16_t powerup_count;
    uint8_t reserved;
    uint32_t seq;
    uint16_t crc;
    
} TBeaconTracker;

typedef struct {
    
    TBeaconHeader hdr;
    uint16_t oid_prox[PROX_MAX];
    uint16_t short_seq;
    uint16_t crc;
    
} TBeaconProx;

typedef union {
    
    TBeaconHeader hdr;
    TBeaconTracker tracker;
    TBeaconProx prox;
    uint32_t block[XXTEA_BLOCK_COUNT];
    uint8_t byte[XXTEA_BLOCK_COUNT * 4];
    
} TBeaconEnvelope;

#endif/*__OPENBEACON_H__*/