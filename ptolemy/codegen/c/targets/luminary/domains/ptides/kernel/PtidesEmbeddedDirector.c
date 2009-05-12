/*** StructDefBlock ***/
#define uint32 unsigned long
#define LED             GPIO_PIN_0 /* PF0 */
#define SELECT          GPIO_PIN_1 /* PF1 */
#define UP              GPIO_PIN_0 /* PE0 */
#define DOWN            GPIO_PIN_1 /* PE1 */
#define LEFT            GPIO_PIN_2 /* PE2 */
#define RIGHT           GPIO_PIN_3 /* PE3 */
#define BUTTON         (UP | DOWN | LEFT | RIGHT)

$super.StructDefBlock();
/**/

/*** FuncProtoBlock ***/
void addStack(void);
unsigned long convertCyclesToNsecs(unsigned long);
unsigned long convertNsecsToCycles(unsigned long);
void die(char*);
void disableInterrupts(void);
void enableInterrupts(void);
void getRealTime(Time*);
void setActuationInterrupt(int);
void setTimedInterrupt(const Time*);
void SysTickHandler(void);
void Timer0IntHandler(void);
void Timer1IntHandler(void);
void __svc(0)  restoreStack();

static char str[40];

$super.FuncProtoBlock();
/**/

/*** FuncBlock ***/

/* hack */

//IntGPIOa and IntGPIOg needs to push currentModelTag onto stack.

// This is the handler for INT_GPIOG. 
// This ISR is used to restore the previous stack after processEvents()
void IntGPIOg(void) {

	GPIOPinIntClear(GPIO_PORTG_BASE, GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 
		| GPIO_PIN_6 | GPIO_PIN_7);
 
	#ifdef LCD_DEBUG
	sprintf(str,"A");
	RIT128x96x4StringDraw(str, 115,0,15);
	#endif

	// do not need to disable interrupts if all interrupts have the same priority
	//disableInterrupts();
    OnePlatform_CompositeActor_SensorInputDevice2();
	// stack manipulation here instead of later.
	addStack();   
}

void IntGPIOa(void) {

	GPIOPinIntClear(GPIO_PORTA_BASE, GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 
		| GPIO_PIN_6 | GPIO_PIN_7);

	#ifdef LCD_DEBUG
	sprintf(str,"A");
	RIT128x96x4StringDraw(str, 115,0,15);
	#endif

	// do not need to disable interrupts if all interrupts have the same priority
	//disableInterrupts();
    OnePlatform_CompositeActor_SensorInputDevice();
	// stack manipulation here instead of later.
	addStack();
}
void Actuation_OnePlatform_CompositeActor_ActuatorOutputDevice(void) {
    //FIXME: add something.
}

void exit(int zero) {
	die("program exit?");
}
/* end of hack */

/* convert between cycles and time */
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

/* error printout */
void die(char *mess) {
        RIT128x96x4Init(2000000);
        RIT128x96x4DisplayOn();
        sprintf(str, mess);
        RIT128x96x4StringDraw(str, 0,90,15);
        return;
}

/* disable and enable interrupts */
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

/* get real time */
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

/* timer */
void setTimedInterrupt(const Time* safeToProcessTime) {

	// it has already been checked, timer always needs to be set, so just set it.
	TimerConfigure(TIMER0_BASE, TIMER_CFG_32_BIT_OS);
    // interrupt 10 times per second
	TimerLoadSet(TIMER0_BASE, TIMER_BOTH, convertNsecsToCycles(safeToProcessTime->nsecs));
	timerInterruptSecsLeft = safeToProcessTime->secs;

	//
	//Setup the interrupts for the timer timeouts
	//
    IntEnable(INT_TIMER0A);
	IntEnable(INT_TIMER0B);
	TimerIntEnable(TIMER0_BASE,TIMER_TIMA_TIMEOUT);
	//TimerIntEnable(TIMER0_BASE,TIMER_TIMB_TIMEOUT);

    // Enable the timers.
    //
    TimerEnable(TIMER0_BASE, TIMER_BOTH);
	return;		
}

void Timer0IntHandler(void) {
    TimerIntClear(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
        if (timerInterruptSecsLeft > 0) {
                TimerLoadSet(TIMER0_BASE, TIMER_BOTH, TIMER_ROLLOVER_CYCLES);
                timerInterruptSecsLeft--;
                return;
        }

        timeSet(MAX_TIME, &lastTimerInterruptTime);
        TimerDisable(TIMER0_BASE, TIMER_BOTH);
        IntDisable(INT_TIMER0A);
        IntDisable(INT_TIMER0B);
        TimerIntDisable(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
        TimerIntDisable(TIMER0_BASE, TIMER_TIMB_TIMEOUT);
        disableInterrupts();
        addStack();
}

/* systick handler */
void SysTickHandler(void) {
        secs++;
}

$super.FuncBlock();

/* Actuators use timer1.
 */
void setActuationInterrupt(int actuatorToActuate) {
 	// If timer already running
	// check if need to reload the interrupt value.
	// If not	
	// Set it up to run.
	//set timer0 as a periodic 32 bit timer
	// FIXME: does TimerValueGet() return 0 if timer is not on?

	int actuatorID;
	Time actuationTime;
	Time actuationLeftOverTime;
	Time physicalTime;

	disableInterrupts();
	
	timeSet(currentModelTime, &actuationTime);

	#ifdef LCD_DEBUG
	sprintf(str,"set act int");
	RIT128x96x4StringDraw(str,0,48,15);
	#endif

	if (actuatorRunning < 0) {
		
		// Timer not running. setup a new timer value and start timer.
//		sprintf(str,"aTIT%d",actuatorTimerInterruptTimes);
//		RIT128x96x4StringDraw(str,   20,70,15);

        // FIXME: move this to intialization.
		TimerConfigure(TIMER1_BASE, TIMER_CFG_A_ONE_SHOT);
		TimerConfigure(TIMER1_BASE, TIMER_CFG_B_ONE_SHOT);
		
	    #ifdef LCD_DEBUG
		sprintf(str,"%d",actuationTime.secs);
		RIT128x96x4StringDraw(str,   100,60,15);
		sprintf(str,"%d",actuationTime.nsecs);
		RIT128x96x4StringDraw(str,   0,60,15);
		#endif

		IntEnable(INT_TIMER1A);
		IntEnable(INT_TIMER1B);
		TimerIntEnable(TIMER1_BASE,TIMER_TIMA_TIMEOUT);
		
		// FIXME: there might be a concurrency issue here, actuatorTimerInterrupt is set to true,
		// yet we could have another interrupt coming in right after it that tries to set another timer interrupt,
		// in which case it would try to access the else{} part of this function.
		// FIXED: this is taken care by all the interrupts having the same priority, thus within this
		// ISR there wouldn't be any preemption.
		actuatorRunning = actuatorToActuate;
		timeSet(actuationTime, &lastActuateTime);

		getRealTime(&physicalTime);
        // FIXME: actually missed a deadline, but sets actuation signal anyway.
		if (timeSub(actuationTime, physicalTime, &actuationLeftOverTime) < 0) {
		    TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
		    actuatorTimerInterruptSecsLeft = 0;
        } else {
		    TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
		    actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
        }
	    TimerEnable(TIMER1_BASE, TIMER_BOTH);

	} else {	// the timer is already running

		// Timer already running, check to see if we need to reload timer value.
		if (timeCompare(lastActuateTime, actuationTime) == MORE) {
			#ifdef LCD_DEBUG
			RIT128x96x4StringDraw("replacing timer", 50,64,15);
			#endif

			TimerDisable(TIMER1_BASE, TIMER_BOTH);
			// replace timer. First, update the actuatorTimeValues of the queue.
			// put it at the beginning of the queue.
			// actuatorRunning is the ID of the last running actuator
			// now insert the time into the queue of waiting timers for this particular actuator.
			// if this actuator currently have more than one waiting time, update the head
			// insert this timer to the previous element of where the head is pointing right now.
			if (actuatorArrayHeadPtrs[actuatorRunning] == 0) {
				actuatorArrayHeadPtrs[actuatorRunning] = MAX_ACTUATOR_TIMER_VALUES - 1;
			} else {
				actuatorArrayHeadPtrs[actuatorRunning]--;	
			}
			// set the head to the previous lastActuateTime.
			timeSet(lastActuateTime, &(actuatorTimerValues[actuatorRunning][actuatorArrayHeadPtrs[actuatorRunning]]));
			actuatorArrayCounts[actuatorRunning]++;

            // replace timer.
			actuatorRunning = actuatorToActuate;
			timeSet(actuationTime, &lastActuateTime);

			getRealTime(&physicalTime);
            if (timeSub(actuationTime, physicalTime, &actuationLeftOverTime) < 0) {
		        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
		        actuatorTimerInterruptSecsLeft = 0;
            } else {
		        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
		        actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
            }
	    	TimerEnable(TIMER1_BASE, TIMER_BOTH);
		} else {
			// we don't need to reload the timer, but we need to put this event into the end of the queue.
			// actuatorToActuate is now the ID of the actuator
			// FIXME: this assumes each actuator has only one input port...
			timeSet(actuationTime, &(actuatorTimerValues[actuatorToActuate][actuatorArrayTailPtrs[actuatorToActuate]]));
			actuatorArrayCounts[actuatorToActuate]++;
			
			actuatorArrayTailPtrs[actuatorToActuate]++;
			if (actuatorArrayTailPtrs[actuatorToActuate] == MAX_ACTUATOR_TIMER_VALUES) {
				actuatorArrayTailPtrs[actuatorToActuate] = 0;	
			}
            //sprintf(str, "AAC: %d", actuatorArrayCounts[i]);
            //RIT128x96x4StringDraw(str, 0,80,15);
		}
	}

	// actuatorArrayHead/TailPtrs/Counts are only added here, so check that we didn't have a overflow
	for (actuatorID = 0; actuatorID < numActuators; actuatorID++) {
		if (actuatorArrayHeadPtrs[actuatorID] == actuatorArrayTailPtrs[actuatorID] 
				&& actuatorArrayCounts[actuatorID] != 0) {
			die("MAX_ACTUATOR_TIMER_VALUES is not large enough.");
		}
		if (actuatorArrayHeadPtrs[actuatorID] != actuatorArrayTailPtrs[actuatorID] 
				&& actuatorArrayCounts[actuatorID] == 0) {
			die("something wrong with actuator ptr algorithm.");
		}
	}
	enableInterrupts();

 //	RIT128x96x4StringDraw("endSetActuator",   20,90,15);
	
}

void Timer1IntHandler(void) {

	int i;
	Time physicalTime;
		
	#ifdef LCD_DEBUG
	sprintf(str, "Act int: %d", actuatorRunning);
	RIT128x96x4StringDraw(str, 20, 72, 15);
	#endif

    // Clear the timer interrupt.
    //
    TimerIntClear(TIMER1_BASE, TIMER_TIMA_TIMEOUT);	
//	RIT128x96x4Clear();
	if (actuatorTimerInterruptSecsLeft > 0) {
		actuatorTimerInterruptSecsLeft--;
		// setup this timer to run once more
		// FIXME: do I need to do enable or disable or something?
		TimerLoadSet(TIMER1_BASE, TIMER_BOTH, TIMER_ROLLOVER_CYCLES);
		return;
	}

  	//GPIOPinWrite(GPIO_PORTB_BASE,GPIO_PIN_7,GPIO_PIN_7*out);
   	//GPIOPinWrite(GPIO_PORTA_BASE,GPIO_PIN_7,GPIO_PIN_7*out);
	#ifdef LCD_DEBUG
	sprintf(str, "Act:%d", actuatorRunning);
	RIT128x96x4StringDraw(str, 80, 24, 15);
	#endif

    // run the actuator actuation function to assert the output signal.
    actuatorActuations[actuatorRunning]();
	
	// When the timer returns to signal a new interrupt has been written, we need to check to see if we have more interrupts
	timeSet(MAX_TIME, &lastActuateTime);
	for (i = 0; i < numActuators; i++) {
		if (actuatorArrayCounts[i] > 0){
//sprintf(str, "%d", actuatorTimerValues[i][actuatorArrayHeadPtrs[i]].secs);
//RIT128x96x4StringDraw(str, 0, 80, 15);
			if (timeCompare(actuatorTimerValues[i][actuatorArrayHeadPtrs[i]], lastActuateTime) == LESS) {
				timeSet(actuatorTimerValues[i][actuatorArrayHeadPtrs[i]], &lastActuateTime);
				actuatorRunning = i;
			}
		}
	}

	#ifdef LCD_DEBUG
	sprintf(str, "To Act:%d", lastActuateTime.secs);
	RIT128x96x4StringDraw(str, 0, 88, 15);
	sprintf(str, "To Act:%d", lastActuateTime.nsecs);
	RIT128x96x4StringDraw(str, 0, 80, 15);
	#endif

	if (timeCompare(lastActuateTime, MAX_TIME) != EQUAL) {
        // there is another actuation to do.
		Time actuationLeftOverTime;

		#ifdef LCD_DEBUG
		RIT128x96x4StringDraw("next actuator", 0,72,15);
		#endif
		//Setup the interrupts for the timer timeouts
		//
	    //IntEnable(INT_TIMER1A);
		//IntEnable(INT_TIMER1B);
		TimerIntEnable(TIMER1_BASE,TIMER_TIMA_TIMEOUT);
		
		// one less off of the queue.
		actuatorArrayHeadPtrs[actuatorRunning]++;
		if (actuatorArrayHeadPtrs[actuatorRunning] == MAX_ACTUATOR_TIMER_VALUES) {
			actuatorArrayHeadPtrs[actuatorRunning] = 0;
		}
		actuatorArrayCounts[actuatorRunning]--;

		// FIXME: there might be a concurrency issue here, actuatorTimerInterrupt is set to true,
		// yet we could have another interrupt coming in right after it that tries to set another timer interrupt,
		// in which case it would try to access the else{} part of this function.
		// FIXED: this is taken care by all the interrupts having the same priority, thus within this
		// ISR there wouldn't be any preemption.

		getRealTime(&physicalTime);
        if (timeSub(lastActuateTime, physicalTime, &actuationLeftOverTime) < 0) {
            TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
            actuatorTimerInterruptSecsLeft = 0;
        } else {
            TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
	        actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
        }
	    TimerEnable(TIMER1_BASE, TIMER_BOTH);
	} else {

        // no more actuation needed at this time.
		actuatorRunning = -1;
		// disable the timer
		TimerDisable(TIMER1_BASE, TIMER_BOTH);
	    IntDisable(INT_TIMER1A);
		IntDisable(INT_TIMER1B);
		//FIXME: is this correct?
		TimerIntDisable(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
		TimerIntDisable(TIMER1_BASE, TIMER_TIMB_TIMEOUT); 
	}
}
/**/

/*** initPDBlock***/
// the platform dependent initialization code goes here.
initializeGPIO();
initializeTimers();
initializeInterrupts();
initializeSystemClock();
/**/

/*** initPDCodeBlock ***/
void initializeTimers(void) {

        SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER0);
        SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER1);

        IntPrioritySet(INT_TIMER0A, 0x00);
        IntPrioritySet(INT_TIMER0B, 0x00);
        IntPrioritySet(INT_TIMER1A, 0x00);
        IntPrioritySet(INT_TIMER1B, 0x00);
}
void initializeSystemClock() {
	SysCtlClockSet(SYSCTL_SYSDIV_1 | SYSCTL_USE_OSC | SYSCTL_OSC_MAIN |
	       SYSCTL_XTAL_8MHZ);

	TIMER_ROLLOVER_CYCLES = SysCtlClockGet();
	
	SysTickPeriodSet(TIMER_ROLLOVER_CYCLES);  
	SysTickEnable();
	IntEnable(FAULT_SYSTICK);  //sys tick vector
}
void initializeInterrupts(void) {
    //FIXME;
}
/**/

/*** initializeGPInput($pad, $pin) ***/
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIODirModeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_DIR_MODE_IN); 
GPIOPinTypeGPIOInput(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
GPIOIntTypeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_RISING_EDGE);  // to set rising edge
GPIOPinIntEnable(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
/**/

/*** initializeGPOutput($pad, $pin) ***/
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIOPadConfigSet(GPIO_PORT$pad_BASE,GPIO_PIN_$pin,GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPD); 
GPIODirModeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_DIR_MODE_OUT); 
GPIOPinTypeGPIOOutput(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
/**/

/*** preinitPDBlock($director, $name) ***/
/**/

/*** wrapupPDBlock() ***/
/**/

