/***preinitBlock***/
//	Based on dropTime (time it took for the ball to pass through the sensors)
//	return the time it will take for the ball to reach the disk
//	Return 0 if dropTime is out-of-bound
uint32 dropTimeToImpactTime(const uint32 dropTime){
	uint32 tableIndex = (dropTime-timeToDisc_offset)>>timeToDisc_shift;
	if(tableIndex > timeToDisc_size)	//Error check, if index is larger than table
		return 0;
		
	return timeToDisc[tableIndex];
}
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
static uint32 dropCount = 0;										//Number of drop sensor events received
static uint32 previousEventTimestamp = 0;
const uint32 currentEventTimestamp = getRealTime();									

//GPIOPinIntClear(DROP_BASE, GPIOPinIntStatus(DROP_BASE, 0));			// Clear the interrupt

dropCount++;
if((dropCount & 0x01) == 0){	//even drop count
	const uint32 dropTime = currentEventTimestamp - previousEventTimestamp;		// Time it took ball to pass through both sensors
	const uint32 timeToImpact = dropTimeToImpactTime(dropTime);					// Time ball will be in the air
	g_impactTime = systemTime() + timeToImpact;	

	//FIXME: If dropTime is out of range, dropCount may be erroneous and should be corrected	
    // send an dummy value out of its output port.
	$put(actuation#0, trajectoryCorrectionPeriod(timeToImpact));
}
previousEventTimestamp = currentEventTimestamp;
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
IntEnable(INT_GPIO$pad);
/**/

/*** sensingBlock($sensorFireMethod, $pad, $pin) ***/
GPIOPinIntClear(GPIO_PORT$pad_BASE, GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6 | GPIO_PIN_7);
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
$sensorFireMethod();
// stack manipulation here instead of later.
addStack();
/**/
