
#ifndef CONFIG_H
#define CONFIG_H

#define CONFIG_DEBUG_LED
#define CONFIG_DELAYUS

/***************************************************************
 *
 *  CPU peripherals settings
 *
 * configuration:
 *      RA0     TX_POWER        ouptput
 *      RA1     SENSOR          input
 *      RA2     nRF_IRQ         input
 *      RA3     Vpp             input
 *      RA4                     input
 *      RA5                     input
 *
 *      RC0     SCK             output
 *      RC1     MOSI            output
 *      RC2     MISO            input
 *      RC3     CSN             output
 *      RC4     nRF_CE          output
 *      RC5     LED             output
 *
 ***************************************************************/

#define CONFIG_PIN_TX_POWER	RA0
#define CONFIG_PIN_SENSOR	RA1
#define CONFIG_PIN_IRQ		RA2

#define CONFIG_PIN_SCK		RC0
#define CONFIG_PIN_MOSI		RC1
#define CONFIG_PIN_MISO		RC2
#define CONFIG_PIN_CSN		RC3
#define CONFIG_PIN_CE		RC4
#define CONFIG_PIN_LED		RC5

#define CONFIG_CPU_FREQ		8000000
#define CONFIG_CPU_PORTA	0x00
#define CONFIG_CPU_PORTC	0x00
#define CONFIG_CPU_TRISA	0x04
#define CONFIG_CPU_TRISC	0x04
#define CONFIG_CPU_ANSELA	0x00
#define CONFIG_CPU_ANSELC	0x00
#define CONFIG_CPU_WPUA		0x00
#define CONFIG_CPU_WPUC		0x00
#define CONFIG_CPU_CMCON0	0x07
#define CONFIG_CPU_OPTION	0x00
#define CONFIG_CPU_OSCCON_SLOW	(0xF<<3)
#define CONFIG_CPU_OSCCON_FAST	((1<<7)|(0xE<<3))

/***************************************************************
 *
 *  RF chip settings
 *
 ***************************************************************/

#define ALLOWED_MAX_CHANNEL 81

#undef  CONFIG_HIRES_LOCATION

#ifdef  CONFIG_HIRES_LOCATION
#define CONFIG_MAX_POWER_LEVELS 8
#else /*CONFIG_HIRES_LOCATION */
#define CONFIG_MAX_POWER_LEVELS 4
#endif /*CONFIG_HIRES_LOCATION */

#endif
