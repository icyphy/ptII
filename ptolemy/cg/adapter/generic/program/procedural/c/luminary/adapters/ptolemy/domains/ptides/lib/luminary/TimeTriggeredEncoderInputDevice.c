/***preinitBlock***/
int32 encoderInputInterruptStatus;
static volatile Disc g_disc;
/**/

/*** sharedBlock ***/
#define ENCODER_PERIF			SYSCTL_PERIPH_GPIOB
#define ENCODER_INT				INT_GPIOB
#define ENCODER_BASE 			GPIO_PORTB_BASE
#define ENCODER_PIN_A			GPIO_PIN_4			// Set B[4] (PB4/C0+) as encoder input (encoder channel A)
#define ENCODER_PIN_B			GPIO_PIN_6			// Set B[6] (PB6/C0-) as encoder input (encoder channel B)
#define ENCODER_PIN_I			GPIO_PIN_2			// Set B[2] (PB2/SCL) as encoder input (encoder channel I)

/**/

/*** initBlock ***/
/**/

/*** fireBlock ***/
static int8 discAligned = 0;
if ($hasToken(trigger#0) || $hasToken(trigger#1)) {
	$put(output, g_disc.position);
} else {
	// Encoder pulse detected - record period (for rate)
	// and increment encoder count
	if (encoderInputInterruptStatus & ENCODER_PIN_A){
		static Time timeGap;
		const uint32 pinStatus = encoderInputInterruptStatus >> 8; //If encoder channel B is leading, then direction is negative
		g_disc.position += pinStatus ? -1 : 1;
		if (-1 == timeSub(currentModelTime, previousEncoderEventTime, &timeGap)) {
			die("timestamps from encoder are backwards");
		}
		// g_disc.period = timeGap.nsecs;
		previousEncoderEventTime = currentModelTime;
	}
	//Encoder alignment detected - occurs twice per rotation.
	//when aligning, set position to zero
	if (!discAligned) {
		g_disc.position = 0;
	}
	if (!discAligned && (encoderInputInterruptStatus & ENCODER_PIN_I)){
		discAligned = 1;
		trajectoryEnable();
	} else if (encoderInputInterruptStatus & ENCODER_PIN_I){
		g_disc.position = g_disc.position & 0xfffffffc;
		g_positionAtHole  = g_disc.position;
	}
}
/**/

/*** initializeGPInput($pad, $pin) ***/
// initialization for GPInput$pad$pin
// first disable GPIO
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIOPinIntClear(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
IntDisable(INT_GPIO$pad);
GPIOPinIntDisable(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
SysCtlPeripheralDisable(SYSCTL_PERIPH_GPIO$pad);
// then configure GPIO
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIODirModeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_DIR_MODE_IN);
GPIOPinTypeGPIOInput(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
IntPrioritySet(INT_GPIO$pad, 0x40);
GPIOIntTypeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_RISING_EDGE);  // to set rising edge
GPIOPinIntEnable(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);

//Channel B - read by ISR for channel A
GPIODirModeSet(ENCODER_BASE, ENCODER_PIN_B, GPIO_DIR_MODE_IN);
GPIOPinTypeGPIOInput(ENCODER_BASE, ENCODER_PIN_B);

//Channel I - alignment pulse
GPIODirModeSet(ENCODER_BASE, ENCODER_PIN_I, GPIO_DIR_MODE_IN);
GPIOPinTypeGPIOInput(ENCODER_BASE, ENCODER_PIN_I);
GPIOIntTypeSet(ENCODER_BASE, ENCODER_PIN_I, GPIO_RISING_EDGE);

IntEnable(INT_GPIO$pad);
/**/

/*** sensingBlock($sensorFireMethod, $pad, $pin) ***/
#ifdef LCD_DEBUG
    debugMessage("$pad$pin");
#endif

saveState();
// need to push the currentModelTag onto the stack.
stackedModelTagIndex++;
if (stackedModelTagIndex > MAX_EVENTS) {
    die("MAX_EVENTS too small for stackedModelTagIndex");
}
executingModelTag[stackedModelTagIndex].microstep = currentMicrostep;
executingModelTag[stackedModelTagIndex].timestamp = currentModelTime;

// for sensing purposes, set the current time to the physical time.
getRealTime(&currentModelTime);
currentMicrostep = 0;

encoderInputInterruptStatus = GPIOPinIntStatus(ENCODER_BASE, 0);
// Clear the interrupt
GPIOPinIntClear(ENCODER_BASE, encoderInputInterruptStatus);
GPIOPinIntClear(ENCODER_BASE, GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6 | GPIO_PIN_7);

encoderInputInterruptStatus |= (GPIOPinRead(ENCODER_BASE, ENCODER_PIN_B) << 8);

// do not need to disable interrupts if all interrupts have the same priority
//disableInterrupts();
$sensorFireMethod();
// stack manipulation here instead of later.
addStack();
/**/
