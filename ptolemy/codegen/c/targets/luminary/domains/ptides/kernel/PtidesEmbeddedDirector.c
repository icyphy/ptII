/*** sharedPDBlock ***/
	/* Status LED and Push Buttons pin definitions */
	#define LED             GPIO_PIN_0 /* PF0 */
	#define SELECT          GPIO_PIN_1 /* PF1 */
	#define UP              GPIO_PIN_0 /* PE0 */
	#define DOWN            GPIO_PIN_1 /* PE1 */
	#define LEFT            GPIO_PIN_2 /* PE2 */
	#define RIGHT           GPIO_PIN_3 /* PE3 */
	#define BUTTON         (UP | DOWN | LEFT | RIGHT)
/**/

/*** initPDBlock***/
// DELETEME: should not be here, should be at domain
initPIBlock();
initPDBlock();
/**/

/*** initHWBlock***/
/**/


/*** initPDCodeBlock ***/
/*** init timer ***/
void initializeTimers(void) {

 	SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER0);
	SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER1);
	
	IntPrioritySet(INT_TIMER0A, 0x00);
	IntPrioritySet(INT_TIMER0B, 0x00);
	IntPrioritySet(INT_TIMER1A, 0x00);
	IntPrioritySet(INT_TIMER1B, 0x00);
}
/**/
/**/

/*** preinitPDBlock($director, $name) ***/

/*** convert between cycles and time ***/
unsigned long convertCyclesToNsecs(unsigned long cycles) {
	// FIXME: assumes the sytem clock runs at 8MHz.
	unsigned long nsecs = cycles << 7;
	nsecs -= (cycles << 2);
	nsecs += cycles;
	return nsecs;
}

unsigned long convertNsecsToCycles(unsigned long nsecs) {
	unsigned long y, z;
	// cycles = nsecs / 128 * 128/125
	// y = nsecs / 128
	// cycles = y * 1.024
	// cycles = y + z
	// let z = .024 * y  ~== 3/128 * y
	// let x = y / 128
	// z = x * 3
	// z = x * 4 - x
	y = nsecs >> 7;
	z = ((y >> 7) << 2) - (y >> 7);
	return y + z; 
}
/**/

/*** error printout ***/
void die(char *mess) {
	RIT128x96x4Init(2000000);
	RIT128x96x4DisplayOn();
	sprintf(str, mess);
	RIT128x96x4StringDraw(str, 0,90,15);
	return; 
}
/**/

/*** disable and enable interrupts ***/
void disableInterrupts() {
	IntMasterDisable();
	return;
}
void enableInterrupts() {
	IntMasterEnable();
    //IntEnable(INT_UART0);
    //UARTIntEnable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	return;
} 
/**/

/*** get real time ***/
void getRealTime(Time* physicalTime) {  

	unsigned long tick1,tick2,tempSecs;
	
	while (TRUE) {
		tick1 = SysTickValueGet();
		tempSecs = secs;
		tick2 = SysTickValueGet();
		if(tick2 < tick1) {  
            // second has been incremented so (I think the counter starts high and counts down)
			physicalTime->secs = tempSecs;
			tick2 = TIMER_ROLLOVER_CYCLES - tick2;
			physicalTime->nsecs = convertCyclesToNsecs(tick2);
			break;
		}
	}
}
/**/

/*** timer ***/
void Timer0IntHandler(void) {
    TimerIntClear(TIMER0_BASE, TIMER_TIMA_TIMEOUT);	
	if (timerInterruptSecsLeft > 0) {
		TimerLoadSet(TIMER0_BASE, TIMER_BOTH, TIMER_ROLLOVER_CYCLES);
		timerInterruptSecsLeft--;
		return;
	}

	timeSet(&lastTimerInterruptTime, &MAX_TIME);
	TimerDisable(TIMER0_BASE, TIMER_BOTH);
    IntDisable(INT_TIMER0A);
	IntDisable(INT_TIMER0B);
	TimerIntDisable(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
	TimerIntDisable(TIMER0_BASE, TIMER_TIMB_TIMEOUT);
	disableInterrupts();																 	
	addStack();
}
/**/

/*** time manipulation ***/
void timeAdd(Time* time1, Time* time2, Time* timeSum) {
	timeSum->secs = time1->secs + time2->secs;
	timeSum->nsecs = time1->nsecs + time2->nsecs;
	if (timeSum->nsecs > 1000000000) {
		timeSum->nsecs -= 1000000000;
		timeSum->secs++;
	}
}
int timeCompare(Time* time1, Time* time2) {
	if (time1->secs < time2->secs) {
		return -1;
	} else if (time1->secs == time2->secs && time1->nsecs < time2->nsecs) {
		return -1;
	} else if (time1->secs == time2->secs && time1->nsecs == time2->nsecs) {
		return 0;
	}
	return 1;	
}
void timeSet(Time* time, Time* refTime) {
	time->secs = refTime->secs;
	time->nsecs = refTime->nsecs;
}
void timeSub(Time* time1, Time* time2, Time* timeSub) {
	if (timeCompare(time1, time2) == -1) {
		die("cannot subtract");
	}
	timeSub->secs = time1->secs - time2->secs;
	if (time1->nsecs < time2->nsecs) {
		timeSub->secs--;
		timeSub->nsecs = time1->nsecs + 1000000000 - time2->nsecs;
	} else {
		timeSub->nsecs = time1->nsecs - time2->nsecs;
	}
}
/**/

/*** systick handler ***/
void SysTickHandler(void) {
	secs++;
}
/**/

/**/

/*** wrapupPDBlock() ***/
/**/

