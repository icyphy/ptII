/*** sharedBlock ***/
/* Standard includes. */
#include <stdio.h>

/* Hardware library includes. */
#include "../../../boards/hw_ints.h"
#include "../../../boards/hw_memmap.h"
#include "../../../boards/hw_types.h"
#include "../../../src/debug.h"
#include "../../../src/gpio.h"
#include "../../../src/interrupt.h"
#include "../../../src/sysctl.h"
#include "../../../src/uart.h"
#include "../rit128x96x4.h"
#include "../../../src/timer.h"
#include "../../../src/systick.h"
#include "../../../boards/hw_nvic.h"
#include "ethernet.h"

#include "statics.h"
#include "globals.h"
#include <debug.h>
/**/

/*** initBlock ***/
GPIOConfig();
initializeInterrupts();
initializeTimers();
/**/

