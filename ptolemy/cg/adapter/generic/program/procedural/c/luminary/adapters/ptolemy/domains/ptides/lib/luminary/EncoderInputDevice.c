/***preinitBlock***/
//Global variables
static volatile uint32 g_alignCount = 0;				// Number of alignment pulses received
static volatile uint8 g_alignEnabled = 0;				// Indicates if alignment is enabled

/**/

/*** sharedBlock ***/
#define ENCODER_PERIF			SYSCTL_PERIPH_GPIOB
#define ENCODER_INT				INT_GPIOB
#define ENCODER_BASE 			GPIO_PORTB_BASE
#define ENCODER_PIN_A			GPIO_PIN_4			// Set B[4] (PB4/C0+) as encoder input (encoder channel A)
#define ENCODER_PIN_B			GPIO_PIN_6			// Set B[6] (PB6/C0-) as encoder input (encoder channel B)
#define ENCODER_PIN_I			GPIO_PIN_2			// Set B[2] (PB2/SCL) as encoder input (encoder channel I)

//Number of encoder pulses per revolution of the disc; this takes into account gearing ratio
#define ENCODER_TICKS_PER_REV	1000

#define DISC_SMALLEST_RATE		(~(1 << 30))	// Smallest rate (closest to zero) corresponds to the largest encoder period
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
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
IntPrioritySet(INT_GPIO$pad, 0x00);
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

static Time previousEncoderEventTime = 0;
int32 interruptStatus;

#ifdef LCD_DEBUG
    debugMessage("$pad$pin");
#endif

// need to push the currentModelTag onto the stack.
executingModelTag[numStackedModelTag].microstep = currentMicrostep;
timeSet(currentModelTime, &(executingModelTag[numStackedModelTag].timestamp));
numStackedModelTag++;
if (numStackedModelTag > MAX_EVENTS) {
    die("MAX_EVENTS too small for numStackedModelTag");
}

// for sensing purposes, set the current time to the physical time.
getRealTime(&currentModelTime);
currentMicrostep = 0;

// do not need to disable interrupts if all interrupts have the same priority
//disableInterrupts();

interruptStatus = GPIOPinIntStatus(ENCODER_BASE, 0);

// Clear the interrupt
GPIOPinIntClear(ENCODER_BASE, interruptStatus);

//Encoder pulse detected - record period (for rate)
// and increment encoder count
if(interruptStatus & ENCODER_PIN_A){
    static Time timeGap;
    const int32 pinStatus = GPIOPinRead(ENCODER_BASE, ENCODER_PIN_B);	//If encoder channel B is leading, then direction is negative
    g_disc.position += pinStatus ? -1 : 1;
    timeSub(currentModelTime, previousEncoderEventTime, &timeGap);
    //FIXME: get rid of the division.
    g_disc.period = timeGap.nsecs / 1000;
	timeSet(currentModelTime, &previousEncoderEventTime);
}
//Encoder alignment detected - occurs twice per rotation.
//when aligning, set position to zero
if(g_alignEnabled && (interruptStatus & ENCODER_PIN_I)){
	g_alignCount++;
	g_disc.position = 0;
}

// stack manipulation here instead of later.
addStack();
/**/
