/***preinitBlock***/
// values generated using z0=532mm, sensorDistance=30mm
const uint32 timeToDisc[timeToDisc_size] = {
        181560390, 184578955, 187464966, 190225458, 192867063, 195396028, 197818236, 200139220, 202364186, 204498030,
        206545355, 208510491, 210397509, 212210239, 213952286, 215627044, 217237705, 218787281, 220278607, 221714356,
        223097052, 224429075, 225712673, 226949969, 228142971, 229293576, 230403582, 231474688, 232508506, 233506563,
        234470307, 235401110, 236300278, 237169050, 238008601, 238820052, 239604467, 240362859, 241096193, 241805389,
        242491323, 243154831, 243796709, 244417720, 245018589, 245600011, 246162651, 246707143, 247234093, 247744085,
        248237673, 248715392, 249177751, 249625242, 250058334, 250477477, 250883105, 251275633, 251655460, 252022969,
        252378529, 252722494, 253055207, 253376994, 253688171, 253989043, 254279903, 254561033, 254832704, 255095181,
        255348715, 255593550, 255829923, 256058060, 256278182, 256490500, 256695219, 256892538, 257082647, 257265733,
        257441973, 257611542, 257774607, 257931331, 258081870, 258226376, 258364998, 258497877, 258625153, 258746960,
        258863427, 258974681, 259080844, 259182036, 259278370, 259369960, 259456914, 259539337, 259617332, 259690998,
        259760432, 259825728, 259886978, 259944269, 259997690, 260047322, 260093250, 260135551, 260174303, 260209582,
        260241461, 260270012, 260295303, 260317402, 260336376, 260352289, 260365204, 260375181, 260382279, 260386558,
        260388074};

//        Based on dropTime (time it took for the ball to pass through the sensors)
//        return the time it will take for the ball to reach the disk
//        Return 0 if dropTime is out-of-bound
uint32 dropTimeToImpactTime(const uint32 dropTime){
        uint32 tableIndex = (dropTime-timeToDisc_offset) >> timeToDisc_shift;
        if(tableIndex > timeToDisc_size) {        //Error check, if index is larger than table
                IntMasterDisable();
                debugMessageNumber("index = ", tableIndex);
                while(1);
        }

        return timeToDisc[tableIndex];
}
/**/

/*** sharedBlock ***/
//Drop sensor
#define DROP_PERIF                                SYSCTL_PERIPH_GPIOG
#define DROP_INT                                INT_GPIOG
#define DROP_BASE                                GPIO_PORTG_BASE
#define DROP_PIN                                GPIO_PIN_0                        // Set G[0] (PG0) as drop sensor input
#define timeToDisc_offset         15000000         //minimum allowed drop time (in ns) for this table; index zero offset
#define timeToDisc_max                 78206188         //maximum allowed drop time (in ns) for this table
#define timeToDisc_shift         19         //amount by which to shift measured drop time to determine table index; log2(dt) (in us)
#define timeToDisc_size         121
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
static Time previousEventTimestamp;
static Time dropTime;
// Time it took ball to pass through both sensors
if (-1 == timeSub(currentModelTime, previousEventTimestamp, &dropTime)) {
        die("timestamp at dropsensor are backwards");
}
previousEventTimestamp = ZERO_TIME;

//GPIOPinIntClear(DROP_BASE, GPIOPinIntStatus(DROP_BASE, 0));                        // Clear the interrupt

if (dropTime.secs == 0 && dropTime.nsecs < timeToDisc_max && dropTime.nsecs > timeToDisc_offset){                        // dropTime is within the range of times that it could take a ball to drop
    uint32 timeToImpact;

    // dropTimeToImpactTime is in ns.
        timeToImpact = dropTimeToImpactTime(dropTime.nsecs);                                        // Time ball will be in the air

        //FIXME: If dropTime is out of range, dropCount may be erroneous and should be corrected
    // send an dummy value out of its output port.
        $put(output#0, timeToImpact);
}
previousEventTimestamp = currentModelTime;
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
IntEnable(INT_GPIO$pad);
/**/

/*** sensingBlock($sensorFireMethod, $pad, $pin) ***/

/**/
