/*** StructDefBlock ***/
#define LED             GPIO_PIN_0 /* PF0 */
#define SELECT          GPIO_PIN_1 /* PF1 */
#define UP              GPIO_PIN_0 /* PE0 */
#define DOWN            GPIO_PIN_1 /* PE1 */
#define LEFT            GPIO_PIN_2 /* PE2 */
#define RIGHT           GPIO_PIN_3 /* PE3 */
#define BUTTON         (UP | DOWN | LEFT | RIGHT)

/* Hardware library includes. */
#include "hw_ints.h"
#include "hw_memmap.h"
#include "hw_types.h"
#include "debug.h"
#include "gpio.h"
#include "interrupt.h"
#include "sysctl.h"
#include "uart.h"
#include "drivers/rit128x96x4.h"
#include "timer.h"
#include "systick.h"
#include "hw_nvic.h"
#include "ethernet.h"

// Number of cycles until timer rolls over
static unsigned long TIMER_ROLLOVER_CYCLES;

$super.StructDefBlock();
/**/

/*** CommonTypeDefinitions ***/
typedef unsigned long long      uint64;
typedef unsigned long           uint32;
typedef unsigned int            uint16;
typedef unsigned char           uint8;
typedef uint8                   byte;
typedef signed long long        int64;
typedef signed long             int32;
typedef signed int              int16;
typedef signed char             int8;
/**/

/*** FuncProtoBlock ***/
void addStack(void);
void saveState(void);
void loadState(void);
uint32 convertCyclesToNsecs(uint32);
uint32 convertNsecsToCycles(uint32);
void die(char*);
void disableInterrupts(void);
void enableInterrupts(void);
void getRealTime(Time*);
void setActuationInterrupt(int);
void setTimedInterrupt(const Time*);
void SysTickHandler(void);
void Timer0IntHandler(void);
void Timer1IntHandler(void);
void __svc(0)  restoreStack(void);

$super.FuncProtoBlock();
/**/

// If more int's are to be added to the argument of FuncBlock, change the
// maxNumSensorInputs field in PtidesBasicDirector.
/*** FuncBlock($dis1, $dis2, $dis3, $dis4, $dis5, $dis6, $dis7, $dis8,
$en1, $en2, $en3, $en4, $en5, $en6, $en7, $en8) ***/

#ifdef LCD_DEBUG
//Unsigned long to ASCII; fixed maxiumum output string length of 32 characters
char *_ultoa(uint32 value, char *string, uint16 radix){
        char digits[32];
        char * const string0 = string;
        int32 ii = 0;
        int32 n;

        do{
                n = value % radix;
                digits[ii++] = (n < 10 ? (char)n+'0' : (char)n-10+'a');
                value /= radix;
        } while(value != 0);

        while (ii > 0)
                *string++ = digits[--ii];
        *string = 0;
        return string0;
}

//Safely print a debug message to the screen; receive arbitrary length string,
//and print sequential messages on the screen, where each message is written to
//a screen line, and subsequent messages are written to the next line (wraps to the top)
//
//Uses all screenlines from STARTLINE
#define DBG_STARTLINE 2
void debugMessage(char * szMsg){
        static uint16 screenIndex = DBG_STARTLINE * 8;    //Screen line to write message (incremented by 8)
        static uint16 eventCount = 0;                     //Event count (0x00 - 0xFF, incremented by 1)
        static char screenBuffer[36] = {'\0'};

        const uint16 eventCountRadix0 = eventCount >> 4;
        const uint16 eventCountRadix1 = eventCount & 0x0F;
        register uint16 index = 0;

        screenBuffer[0] = eventCountRadix0 < 10 ? '0' + eventCountRadix0 : 'a' + eventCountRadix0 - 10;
        screenBuffer[1] = eventCountRadix1 < 10 ? '0' + eventCountRadix1 : 'a' + eventCountRadix1 - 10;
        screenBuffer[2] = ' ';
        while(index < 32 && szMsg[index]){
                screenBuffer[index+3] = szMsg[index];
                index++;
        }
        screenBuffer[index+3] = '\0';

        disableInterrupts();
        RIT128x96x4StringDraw("                      ", 0, screenIndex, 15);
        RIT128x96x4StringDraw(screenBuffer, 0, screenIndex, 15);

        enableInterrupts();
        screenIndex = screenIndex < 88 ? screenIndex + 8 : DBG_STARTLINE * 8;
        eventCount = (eventCount + 1) & 0xFF;
}

//Print a debug message, with a number appended at the end
void debugMessageNumber(char * szMsg, uint32 num){
        static char szNumberBuffer[32] = {'\0'};
        static char szResult[32] = {'\0'};
        register uint16 index = 0;
        register const char * ptrNum = &szNumberBuffer[0];

        _ultoa(num, szNumberBuffer, 10);
        while(index < 32 && szMsg[index]){
                szResult[index] = szMsg[index];
                index++;
        }
        while(index < 32 && *ptrNum){
                szResult[index] = *ptrNum;
                index++;
                ptrNum++;
        }
        szResult[index] = '\0';

        debugMessage(szResult);
}
#else
    //Debug messages will have no effect
    #define debugMessage(x)
    #define debugMessageNumber(x, y)
#endif

void exit(int zero) {
        die("program exit?");
}

// Convert processor cycle count to nanoseconds.
// This method assumes a fixed clock rate
// of 50 MHz
uint32 convertCyclesToNsecs(uint32 cycles){
    // nsec = cycles * 20 = cycles * (4+1) * 4
    return ((cycles << 2) + cycles) << 2;
}

// Convert nanoseconds to processor cycles.
// This method assumes a fixed clock rate
// of 50 MHz
uint32 convertNsecsToCycles(uint32 nsecs) {
        return nsecs / 20;
        // FIXME: Is there a way to make it less expensive?
}

/* error printout */
void die(char *mess) {
	// FIXME ADD RENESAS CODE
}

// Disable all peripheral and timer interrupts (does not include the systick)
// IntMasterDisable should not be called here, because the systick handler would be disabled.
// Instead, we disable each interrupt individually.
void disableInterrupts(void) {
	// FIXME ADD RENESAS CODE
}
// Enable all peripheral and timer interrupts (does not include the systick)
// IntMasterEnable should not be called here, because the systick handler would be disabled.
// Instead, we disable each interrupt individually.
void enableInterrupts(void) {
	// FIXME ADD RENESAS CODE
}

//Return the real physical time.
//external interrupts should be disabled when calling this function
//(however Systick interrupt should not be disabled.
void getRealTime(Time * const physicalTime){
	// FIXME ADD RENESAS CODE
}

/* timer */
void setTimedInterrupt(const Time* safeToProcessTime) {
	// FIXME ADD RENESAS CODE
}

void Timer0IntHandler(void) {
	// FIXME ADD RENESAS CODE
}

//SysTickHandler ISR configured to execute every 1/4 second (
void SysTickHandler(void) {
	// FIXME ADD RENESAS CODE
}

$super.FuncBlock();

// Actuators use timer1.
void setActuationInterrupt(int actuatorToActuate) {
	// FIXME ADD RENESAS CODE
}

void Timer1IntHandler(void) {
	// FIXME ADD RENESAS CODE
}
/**/

/*** initPDBlock***/
// the platform dependent initialization code goes here.
initializeTimers();
initializePDSystem();
initializeHardware();
initializeInterrupts();
/**/

/*** initPDCodeBlock ***/
void initializeTimers(void) {
	// FIXME ADD RENESAS CODE
}

void initializePDSystem() {
	// FIXME ADD RENESAS CODE
}

void initializeInterrupts(void) {
	// FIXME ADD RENESAS CODE
}
/**/

/*** preinitPDBlock()***/
/**/

/*** wrapupPDBlock() ***/
/**/


/*** mainLoopBlock ***/
void execute() {
	// FIXME ADD RENESAS CODE
}

void processEvents() {
	// FIXME ADD RENESAS CODE
}
/**/
