/***preinitBlock***/
unsigned char $actorSymbol(result) = 0;
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** initializeGPOutput($pad, $pin) ***/
// initialization for GPOutput$pad$pin
// first disable GPIO
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIOPinIntClear(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
IntDisable(INT_GPIO$pad);
GPIOPinIntDisable(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
SysCtlPeripheralDisable(SYSCTL_PERIPH_GPIO$pad);
// then configure GPIO
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIO$pad);
GPIODirModeSet(GPIO_PORT$pad_BASE, GPIO_PIN_$pin,GPIO_DIR_MODE_OUT);
GPIOPadConfigSet(GPIO_PORT$pad_BASE,GPIO_PIN_$pin,GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPD);
GPIOPinTypeGPIOOutput(GPIO_PORT$pad_BASE, GPIO_PIN_$pin);
/**/

/*** fireBlock($actuator) ***/
Time currentPhysicalTime;

getRealTime(&currentPhysicalTime);

// Compare time with tagged time, if not safe to actuate, then add events
// and wait until time to actuate. If safe to actuate, actuate now.
if (timeCompare(currentModelTime, currentPhysicalTime) == MORE) {
        /* This can be used for debugging to determine when events are received
                by an actuator
        if (thisActuator == ACTUATORS[0]) {
                GPIOPinWrite(GPIO_PORTC_BASE, GPIO_PIN_7, GPIO_PIN_7);
        } else if (thisActuator == ACTUATORS[1]) {
                 GPIOPinWrite(GPIO_PORTA_BASE, GPIO_PIN_7, GPIO_PIN_7);
        }*/

        setActuationInterrupt($actuator);

        /*        This clears the GPIO pins after debugging information has been set
                FIXME: This is extraneous. This can be removed.
        if (thisActuator == ACTUATORS[0]) {
                GPIOPinWrite(GPIO_PORTC_BASE, GPIO_PIN_7, 0);
        } else if (thisActuator == ACTUATORS[1]) {
                 GPIOPinWrite(GPIO_PORTA_BASE, GPIO_PIN_7, 0);
        }*/

} else {
        // FIXME: do something!
        /*        Debug - write to GPIO on deadline miss
        GPIOPinWrite(GPIO_PORTC_BASE,GPIO_PIN_7,GPIO_PIN_7);
        while (i < 999) {
                i++;
        }*/

        #ifdef LCD_DEBUG
    debugMessage("dead miss");
        #endif
}
/**/

/*** actuationBlock($pad, $pin) ***/
$actorSymbol(result) ^= 0xFF;        //Toggle pin value
GPIOPinWrite(GPIO_PORT$pad_BASE, GPIO_PIN_$pin, GPIO_PIN_$pin & $actorSymbol(result));
/**/
