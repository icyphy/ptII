/***preinitBlock***/
static volatile Time g_impactTime;			// System time that a ball will impact the disc

// values generated using z0=532mm, sensorDistance=30mm
//FIXME: Increase the resolution of this table.
const uint32 timeToDisc[timeToDisc_size] = {
	222481, 225081, 227500, 229753, 231854, 233814, 235646, 237358, 238959, 240458, 
	241862, 243177, 244410, 245566, 246650, 247667, 248621, 249516, 250356, 251143, 
	251882, 252575, 253223, 253831, 254401, 254933, 255431, 255896, 256329, 256733, 
	257109, 257459, 257783, 258082, 258359, 258614, 258848, 259062, 259257, 259433, 
	259592, 259735, 259862, 259973, 260070, 260153, 260223, 260279, 260323, 260356, 
	260377, 260387};

//	Based on dropTime (time it took for the ball to pass through the sensors)
//	return the time it will take for the ball to reach the disk
//	Return 0 if dropTime is out-of-bound
uint32 dropTimeToImpactTime(const uint32 dropTime){
	uint32 tableIndex = (dropTime-timeToDisc_offset)>>timeToDisc_shift;
	if(tableIndex > timeToDisc_size)	//Error check, if index is larger than table
		return 0;
		
	return timeToDisc[tableIndex];
}


// Find the change in period needed for the ball to fall through one hole
int32 trajectoryCorrectionPeriod(uint32 impactTime) {
	Disc disc = Disc_0;
	int32 positionProjectedChange;
	int32 positionCorrection;
	
	discState(&disc);	// Record disc position and encoder pulse period
	positionProjectedChange = impactTime / disc.period;
	positionCorrection = (disc.position + positionProjectedChange) % ENCODER_TICKS_PER_REV;
	
	if (positionCorrection < (ENCODER_TICKS_PER_REV / 4))	// If the disk needs to slow down for the ball to fall into the hole at 0 degrees
		return impactTime/(positionProjectedChange - positionCorrection);
	else if(positionCorrection >= (ENCODER_TICKS_PER_REV / 4) && positionCorrection < ((ENCODER_TICKS_PER_REV * 3)/4)) //For the hole at 180 degrees
		return impactTime/(positionProjectedChange + (ENCODER_TICKS_PER_REV / 2) - positionCorrection);
	else	// If the disk needs to speed up for the ball to fall into the hole at 0 degrees
		return impactTime/(positionProjectedChange + ENCODER_TICKS_PER_REV - positionCorrection);
}
/**/

/*** sharedBlock ***/
//Drop sensor
#define DROP_PERIF				SYSCTL_PERIPH_GPIOG
#define DROP_INT				INT_GPIOG
#define DROP_BASE				GPIO_PORTG_BASE
#define DROP_PIN				GPIO_PIN_0			// Set G[0] (PG0) as drop sensor input

#define timeToDisc_offset	25000 	//minimum allowed drop time (in us) for this table; index zero offset
#define timeToDisc_shift	10 		//amount by which to shift measured drop time to determine table index; log2(dt) (in us)
#define timeToDisc_size		52

/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
static uint32 dropCount = 0;										//Number of drop sensor events received
static Time previousEventTimestamp;
static Time currentEventTimestamp;
getRealTime(&currentEventTimestamp);

timeSet(ZERO_TIME, &previousEventTimestamp);

//GPIOPinIntClear(DROP_BASE, GPIOPinIntStatus(DROP_BASE, 0));			// Clear the interrupt

dropCount++;
if((dropCount & 0x01) == 0){	//even drop count
	Time dropTime;
    Time currentSysTime;
    uint32 timeToImpact;
    timeSub(currentEventTimestamp, previousEventTimestamp, &dropTime);		// Time it took ball to pass through both sensors
    if (dropTime.secs > 0) {
        die("system do not support such big drop time");
    }
    // FIXME: don't do divisions or multiplications...
    // dropTimeToImpactTime is in us.
	timeToImpact = dropTimeToImpactTime(dropTime.nsecs / 1000);					// Time ball will be in the air
    getRealTime(&currentSysTime);
    // g_impactTime is the sum of current time and timeToImpact.
    g_impactTime.nsecs = currentSysTime.nsecs + timeToImpact * 1000;
    if (g_impactTime.nsecs > 1000000000) {
        g_impactTime.nsecs -= 1000000000;
        g_impactTime.secs++;
    }

	//FIXME: If dropTime is out of range, dropCount may be erroneous and should be corrected	
    // send an dummy value out of its output port.
	$put(output#0, trajectoryCorrectionPeriod(timeToImpact));
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
