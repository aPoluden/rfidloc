/* Library includes. */
#include <string.h>
#include <stdio.h>

#include <FreeRTOS.h>
#include <AT91SAM7.h>
#include <USB-CDC.h>
#include <task.h>
#include <beacontypes.h>
#include <board.h>
#include <dbgu.h>
#include <env.h>

#include "led.h"
#include "cmd.h"
#include "proto.h"

/**********************************************************************/
static inline void
prvSetupHardware (void)
{
	/*  When using the JTAG debugger the hardware is not always initialised to
	   the correct default state.  This line just ensures that this does not
	   cause all interrupts to be masked at the start. */
	AT91C_BASE_AIC->AIC_EOICR = 0;

	/*  Enable the peripheral clock. */
	AT91C_BASE_PMC->PMC_PCER = 1 << AT91C_ID_PIOA;
	AT91C_BASE_PMC->PMC_PCER = 1 << AT91C_ID_PIOB;

	/* initialize environment variables */
	AT91F_DBGU_Init ();
	env_init ();
	if (!env_load ())
	{
		env.e.mode = 0;
		env.e.reader_id = 1;
		env_store ();
	}
}

/**********************************************************************/
void
vApplicationIdleHook (void)
{
	/* Restart watchdog, has been enabled in Cstartup_SAM7.c */
	AT91F_WDTRestart (AT91C_BASE_WDTC);
}

/**********************************************************************/
void
vDebugSendHook (char data)
{
	vUSBSendByte (data);
}

/**********************************************************************/
void __attribute__ ((noreturn)) mainloop (void)
{
	prvSetupHardware ();

	vLedInit ();

	xTaskCreate (vUSBCDCTask, (signed portCHAR *) "USB", TASK_USB_STACK,
				 NULL, TASK_USB_PRIORITY, NULL);

	//vCmdInit ();

	vInitProtocolLayer ();

	vTaskStartScheduler ();

	while (1);
}
