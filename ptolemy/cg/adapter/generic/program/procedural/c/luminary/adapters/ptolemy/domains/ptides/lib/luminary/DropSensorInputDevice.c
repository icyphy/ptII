/***preinitBlock***/
// values generated using z0=532mm, sensorDistance=30mm
//FIXME: Increase the resolution of this table.
const uint32 timeToDisc[timeToDisc_size] = {
	181524, 184475, 187299, 190004, 192594, 195076, 197456, 199738, 201928, 204031, 
	206049, 207989, 209853, 211644, 213368, 215026, 216622, 218159, 219639, 221065, 
	222439, 223764, 225042, 226274, 227463, 228611, 229719, 230789, 231822, 232820, 
	233785, 234717, 235618, 236489, 237332, 238147, 238935, 239698, 240436, 241150, 
	241841, 242510, 243157, 243784, 244391, 244979, 245549, 246100, 246634, 247151, 
	247652, 248137, 248607, 249062, 249503, 249930, 250343, 250744, 251131, 251507, 
	251871, 252223, 252564, 252894, 253214, 253523, 253822, 254112, 254392, 254663, 
	254925, 255178, 255423, 255660, 255889, 256109, 256323, 256529, 256727, 256919, 
	257104, 257282, 257453, 257619, 257778, 257931, 258078, 258219, 258355, 258485, 
	258610, 258730, 258844, 258954, 259058, 259158, 259254, 259344, 259431, 259512, 
	259590, 259664, 259733, 259798, 259860, 259918, 259972, 260022, 260069, 260112, 
	260152, 260188, 260222, 260252, 260278, 260302, 260323, 260341, 260355, 260367, 
	260377, 260383, 260387, 260388};

static volatile Time g_impactTime;			// System time that a ball will impact the disc

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
//Drop sensor
#define DROP_PERIF				SYSCTL_PERIPH_GPIOG
#define DROP_INT				INT_GPIOG
#define DROP_BASE				GPIO_PORTG_BASE
#define DROP_PIN				GPIO_PIN_0			// Set G[0] (PG0) as drop sensor input

#define timeToDisc_offset 	15000000 	//minimum allowed drop time (in ns) for this table; index zero offset
#define timeToDisc_max 		78206000 	//maximum allowed drop time (in ns) for this table
#define timeToDisc_shift 	9 	//amount by which to shift measured drop time to determine table index; log2(dt) (in us)
#define timeToDisc_size 	124
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
static uint32 dropCount = 0;										//Number of drop sensor events received
static Time previousEventTimestamp;
static Time currentEventTimestamp;
static Time dropTime;
getRealTime(&currentEventTimestamp);
timeSub(currentEventTimestamp, previousEventTimestamp, &dropTime);		// Time it took ball to pass through both sensors
    
timeSet(ZERO_TIME, &previousEventTimestamp);

//GPIOPinIntClear(DROP_BASE, GPIOPinIntStatus(DROP_BASE, 0));			// Clear the interrupt

if (dropTime.nsecs < timeToDisc_max && dropTime.nsecs > timeToDisc_offset){			// dropTime is within the range of times that it could take a ball to drop
    Time currentSysTime;
    uint32 timeToImpact;

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
	$put(output#0, timeToImpact);
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
disableInterrupts();
$sensorFireMethod();
// stack manipulation here instead of later.
addStack();
/**/
