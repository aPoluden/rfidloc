
#include <htc.h>
#include <stdint.h>
#include "config.h"
#include "timer.h"

#define	T1CON_TMR1ON	(1 << 0)
#define T1CON_T1SYNC	(1 << 2)
#define T1CON_T1OSCEN	(1 << 3)
#define T1CON_T1CKPS0	(1 << 4)
#define T1CON_T1CKPS1	(1 << 5)
#define T1CON_T1CKCS0	(1 << 6)
#define T1CON_T1CKCS1	(1 << 7)

/* Configure Timer 1 to use external 32768Hz crystal and
 * no (1:1) prescaler */
#define T1CON_DEFAULT	(T1CON_T1OSCEN | T1CON_T1SYNC | T1CON_T1CKCS1)

static volatile char timer1_wrapped;

void
timer_init (void)
{
	timer1_wrapped = 0;

	T1CON = T1CON_DEFAULT;
	T1GCON = 0;
	TMR1IE = 1;
	PEIE = 1;
	GIE = 1;
	while (!T1OSCR);
}

void
sleep_jiffies (uint16_t jiffies)
{
	jiffies ^= 0xffff;
	TMR1H = jiffies >> 8;
	TMR1L = (unsigned char) jiffies;
	TMR1IF = 0;
	timer1_wrapped = 0;
	TMR1ON = 1;
	while (!timer1_wrapped)
		SLEEP ();
	TMR1ON = 0;
}

void
sleep_deep (uint8_t div)
{
	CLRWDT ();
	WDTCON = ((div & 0x1F) << 1) | 1;
	SLEEP ();
	WDTCON = 0;
	T1CON = T1CON_DEFAULT;
}

void interrupt
irq (void)
{
	/* timer1 has overflowed */
	if (TMR1IF)
	{
		timer1_wrapped = 1;
		TMR1IF = 0;
	}
}
