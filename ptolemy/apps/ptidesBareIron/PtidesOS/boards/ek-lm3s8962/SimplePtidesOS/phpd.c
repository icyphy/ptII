#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <math.h>

#include "../../../hw_ints.h"
#include "../../../hw_memmap.h"
#include "../../../hw_types.h"
#include "../../../src/debug.h"
#include "../../../src/gpio.h"
#include "../../../src/interrupt.h"
#include "../../../src/sysctl.h"
#include "../../../src/uart.h"
#include "../rit128x96x4.h"
#include "../../../src/timer.h"
#include "../../../src/systick.h"
#include "../../../hw_nvic.h"
#include "ethernet.h"

#include "structures.h"
#include "functions.h"
#include "actors.h"

#include "globals.h"
#include "lwip/opt.h"
#include "lwip/def.h"
#include "lwip/sys.h"
#include "ptpd.h"
#include "random.h"

#include "statics.h"

//#include "timer.h"
//#include "clock-arch.h"

// FIXME: constants.h is from PTPd program, which is open source as long as we provide
// the copywrite statement.
#include "dep-lmi/constants_dep.h"
#include "dep-lmi/datatypes_dep.h"
#include "constants.h"
#include "datatypes.h"


#define ETHERNET_INT_PRIORITY 0x80

static volatile unsigned long g_ulSystemTickHigh = 0;

//*****************************************************************************
//
// Local data for clocks and timers.
//
//*****************************************************************************
static volatile unsigned long g_ulNewSystemTickReload = 0;
static volatile unsigned long g_ulSystemTickReload = 0;

//*****************************************************************************
//
// Statically allocated runtime options and parameters for PTPd.
//
//*****************************************************************************
static PtpClock g_sPTPClock;
static ForeignMasterRecord g_psForeignMasterRec[DEFUALT_MAX_FOREIGN_RECORDS];
static RunTimeOpts g_sRtOpts;

//*****************************************************************************
//
// External references.
//
//*****************************************************************************
extern void httpd_init(void);
extern void fs_init(void);
extern void lwip_init(void);
extern void lwip_tick(unsigned long ulTickMS);

//*****************************************************************************
//
// Local function prototypes.
//
//*****************************************************************************
void adjust_rx_timestamp(TimeInternal *psRxTime, unsigned long ulRxTime,
                         unsigned long ulNow);


//*****************************************************************************
//
// Adjust the supplied timestamp to account for interrupt latency.
//
//*****************************************************************************
void adjust_rx_timestamp(TimeInternal *psRxTime, unsigned long ulRxTime,
                    unsigned long ulNow)
{
    unsigned long ulCorrection;

    //
    // Time parameters ulNow and ulRxTime are assumed to have originated from
    // a 16 bit down counter operating over its full range.
    //

    //
    // Determine the number of cycles between the receive timestamp and the
    // point that it was read.
    //
    if(ulNow < ulRxTime)
    {
        //
        // The timer didn't wrap between the timestamp and now.
        //
        ulCorrection = ulRxTime - ulNow;
    }
    else
    {
        //
        // The timer wrapped between the timestamp and now
        //
        ulCorrection = ulRxTime + (0x10000 - ulNow);
    }

    //
    // Convert the correction from cycles to nanoseconds.
    //
    ulCorrection *= TICKNS;

    //
    // Subtract the correction from the supplied timestamp value.
    //
    if(psRxTime->nanoseconds >= ulCorrection)
    {
        //
        // In this case, we need only adjust the nanoseconds value since there
        // is no borrow from the seconds required.
        //
        psRxTime->nanoseconds -= ulCorrection;
    }
    else
    {
        //
        // Here, the adjustment affects both the seconds and nanoseconds
        // fields. The correction cannot be more than 1 second (16 bit counter
        // maximum offset and minimum cycle time of 125nS gives a maximum
        // correction of 8.192mS) so we don't need to perform any nasty, slow
        // modulo calculations here.
        //
        psRxTime->seconds--;
        psRxTime->nanoseconds += (1000000000 - ulCorrection);
    }
}
//*****************************************************************************
//
// Based on the value (adj) provided by the PTPd Clock Servo routine, this
// function will adjust the SysTick periodic interval to allow fine-tuning of
// the PTP Clock.
//
//*****************************************************************************
Boolean	adjFreq(Integer32 adj)
{
    unsigned long ulTemp;

    //
    // Check for max/min value of adjustment.
    //
    if(adj > ADJ_MAX)
    {
        adj = ADJ_MAX;
    }
    else if(adj < -ADJ_MAX)
    {
        adj = -ADJ_MAX;
    }

    //
    // Convert input to nanoseconds / systick.
    //
    adj = adj / SYSTICKHZ;

    //
    // Get the nominal tick reload value and convert to nano seconds.
    //
    ulTemp = (SysCtlClockGet() / SYSTICKHZ) * TICKNS;

    //
    // Factor in the adjustment.
    //
    ulTemp -= adj;

    //
    // Get a modulo count of nanoseconds for fine tuning.
    //
    g_ulSystemTickHigh = ulTemp % TICKNS;

    //
    // Set the reload value.
    //
    g_ulNewSystemTickReload = ulTemp / TICKNS;

    //
    // Return.
    //
    return(TRUE);
}


void IEEE1588Init(void) {

    unsigned long ulUser0, ulUser1;
    unsigned char pucMACArray[8];
    volatile unsigned long ulDelay;

    //
    // Enable and Reset the Ethernet Controller.
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_ETH);
    SysCtlPeripheralReset(SYSCTL_PERIPH_ETH);
    IntPrioritySet(INT_ETH, ETHERNET_INT_PRIORITY);

    //
    // Enable Port F for Ethernet LEDs.
    //  LED0        Bit 3   Output
    //  LED1        Bit 2   Output
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOF);
    GPIODirModeSet(GPIO_PORTF_BASE, GPIO_PIN_2 | GPIO_PIN_3, GPIO_DIR_MODE_HW);
    GPIOPadConfigSet(GPIO_PORTF_BASE, GPIO_PIN_2 | GPIO_PIN_3,
                     GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD);


// we always do hardware timestamping

    //
    // Enable timer 3 to capture the timestamps of the incoming packets.
    //
    HWREGBITW(&g_ulFlags, FLAG_HWTIMESTAMP) = 1;
    SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER3);
    SysCtlPeripheralReset(SYSCTL_PERIPH_TIMER3);

    //
    // Configure Timer 3 as 2 16 bit counters.  Timer B is used to capture
    // the time of the last Ethernet RX interrupt and we leave Timer A free
    // running to allow us to determine how much time has passed between
    // the interrupt firing and the ISR actually reading the packet.  Had
    // we been interested in transmit timestamps, time 3A would have been
    // used for this and a second timer block would be needed to provide
    // the free-running reference count.
    //
    TimerConfigure(TIMER3_BASE, (TIMER_CFG_16_BIT_PAIR |
                                 TIMER_CFG_A_PERIODIC |
                                 TIMER_CFG_B_CAP_TIME));
    TimerPrescaleSet(TIMER3_BASE, TIMER_BOTH, 0);
    TimerLoadSet(TIMER3_BASE, TIMER_BOTH, 0xFFFF);
    TimerControlEvent(TIMER3_BASE, TIMER_B, TIMER_EVENT_POS_EDGE);

    //
    // Start the timers running.
    //
    TimerEnable(TIMER3_BASE, TIMER_BOTH);

// FIXME: what is systick's position in all of this? Do we have to use it to use PTP?

    //
    // Configure SysTick for a periodic interrupt in PTPd system.
    //
    ptpd_systick_init();
    //
    // Enable processor interrupts.
    //
    IntMasterEnable();

    //
    // Configure the hardware MAC address for Ethernet Controller filtering of
    // incoming packets.
    //
    // For the Luminary Micro Evaluation Kits, the MAC address will be stored
    // in the non-volatile USER0 and USER1 registers.  These registers can be
    // read using the FlashUserGet function, as illustrated below.
    //
    FlashUserGet(&ulUser0, &ulUser1);
    if((ulUser0 == 0xffffffff) || (ulUser1 == 0xffffffff))
    {
        //
        // We should never get here.  This is an error if the MAC address has
        // not been programmed into the device.  Exit the program.
        //
        RIT128x96x4StringDraw("MAC Address", 0, 16, 15);
        RIT128x96x4StringDraw("Not Programmed!", 0, 24, 15);
        die("");
    }

    //
    // Convert the 24/24 split MAC address from NV ram into a 32/16 split MAC
    // address needed to program the hardware registers, then program the MAC
    // address into the Ethernet Controller registers.
    //
    pucMACArray[0] = ((ulUser0 >> 0) & 0xff);
    pucMACArray[1] = ((ulUser0 >> 8) & 0xff);
    pucMACArray[2] = ((ulUser0 >> 16) & 0xff);
    pucMACArray[3] = ((ulUser1 >> 0) & 0xff);
    pucMACArray[4] = ((ulUser1 >> 8) & 0xff);
    pucMACArray[5] = ((ulUser1 >> 16) & 0xff);

    //
    // Program the hardware with it's MAC address (for filtering).
    //
    EthernetMACAddrSet(ETH_BASE, pucMACArray);

    //
    // Initialize all of the lwIP code, as needed, which will also initialize
    // the low-level Ethernet code.
    //
    lwip_init();

    //
    // Initialize a sample web server application.
    //
    httpd_init();
}

void ptpd_init(void)
{
    unsigned long ulTemp;

    //
    // Clear out all of the run time options and protocol stack options.
    //
    memset(&g_sRtOpts, 0, sizeof(g_sRtOpts));
    memset(&g_sPTPClock, 0, sizeof(g_sPTPClock));

    //
    // Initialize all PTPd run time options to a valid, default value.
    //
    g_sRtOpts.syncInterval = DEFUALT_SYNC_INTERVAL;
    memcpy(g_sRtOpts.subdomainName, DEFAULT_PTP_DOMAIN_NAME,
           PTP_SUBDOMAIN_NAME_LENGTH);
    memcpy(g_sRtOpts.clockIdentifier, IDENTIFIER_DFLT, PTP_CODE_STRING_LENGTH);
    g_sRtOpts.clockVariance = DEFAULT_CLOCK_VARIANCE;
    g_sRtOpts.clockStratum = DEFAULT_CLOCK_STRATUM;
    g_sRtOpts.clockPreferred = FALSE;
    g_sRtOpts.currentUtcOffset = DEFAULT_UTC_OFFSET;
    g_sRtOpts.epochNumber = 0;
    memcpy(g_sRtOpts.ifaceName, "LMI", strlen("LMI"));
    g_sRtOpts.noResetClock = DEFAULT_NO_RESET_CLOCK;
    g_sRtOpts.noAdjust = FALSE;
    g_sRtOpts.displayStats = FALSE;
    g_sRtOpts.csvStats = FALSE;
    g_sRtOpts.unicastAddress[0] = 0;
    g_sRtOpts.ap = DEFAULT_AP;
    g_sRtOpts.ai = DEFAULT_AI;
    g_sRtOpts.s = DEFAULT_DELAY_S;
    g_sRtOpts.inboundLatency.seconds = 0;
    g_sRtOpts.inboundLatency.nanoseconds = DEFAULT_INBOUND_LATENCY;
    g_sRtOpts.outboundLatency.seconds = 0;
    g_sRtOpts.outboundLatency.nanoseconds = DEFAULT_OUTBOUND_LATENCY;
    g_sRtOpts.max_foreign_records = DEFUALT_MAX_FOREIGN_RECORDS;
    g_sRtOpts.slaveOnly = TRUE;
    g_sRtOpts.probe = FALSE;
    g_sRtOpts.probe_management_key = 0;
    g_sRtOpts.probe_record_key = 0;
    g_sRtOpts.halfEpoch = FALSE;

    //
    // Initialize the PTP Clock Fields.
    //
    g_sPTPClock.foreign = &g_psForeignMasterRec[0];

    //
    // Configure port "uuid" parameters.
    //
    g_sPTPClock.port_communication_technology = PTP_ETHER;
    EthernetMACAddrGet(ETH_BASE, (unsigned char *)g_sPTPClock.port_uuid_field);

    //
    // Enable Ethernet Multicast Reception (required for PTPd operation).
    // Note:  This must follow lwIP/Ethernet initialization.
    //
    ulTemp = EthernetConfigGet(ETH_BASE);
    ulTemp |= ETH_CFG_RX_AMULEN;
    if(HWREGBITW(&g_ulFlags, FLAG_HWTIMESTAMP))
    {
        ulTemp |= ETH_CFG_TS_TSEN;
    }
    EthernetConfigSet(ETH_BASE, ulTemp);

    //
    // Run the protocol engine for the first time to initialize the state
    // machines.
    //
    protocol_first(&g_sRtOpts, &g_sPTPClock);
}


//*****************************************************************************
//
// Initialization code for PTPD software system tick timer.
//
//*****************************************************************************
void ptpd_systick_init(void)
{
    //
    // Initialize the System Tick Timer to run at specified frequency.
    //
    SysTickPeriodSet(SysCtlClockGet() / SYSTICKHZ);

    //
    // Initialize the timer reload values for fine-tuning in the handler.
    //
    g_ulSystemTickReload = SysTickPeriodGet();
    g_ulNewSystemTickReload = g_ulSystemTickReload;

    //
    // Enable the System Tick Timer.
    //
    SysTickEnable();
    SysTickIntEnable();
}

//*****************************************************************************
//
// Run the protocol engine loop/poll.
//
//*****************************************************************************
void ptpd_tick(void)
{
    //
    // Run the protocol engine for each pass through the main process loop.
    //
    protocol_loop(&g_sRtOpts, &g_sPTPClock);
}

