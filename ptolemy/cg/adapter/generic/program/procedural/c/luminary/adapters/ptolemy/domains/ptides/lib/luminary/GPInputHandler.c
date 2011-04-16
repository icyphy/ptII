/***preinitBlock***/
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** fireBlock ***/
// send an dummy value out of its output port.
$put(output#0, 1);
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
stackedModelTagIndex++;
if (stackedModelTagIndex > MAX_EVENTS) {
    die("MAX_EVENTS too small for stackedModelTagIndex");
}
executingModelTag[stackedModelTagIndex].microstep = currentMicrostep;
executingModelTag[stackedModelTagIndex].timestamp = currentModelTime;

// for sensing purposes, set the current time to the physical time.
getRealTime(&currentModelTime);
currentMicrostep = 0;

// do not need to disable interrupts if all interrupts have the same priority
//disableInterrupts();
$sensorFireMethod();
// stack manipulation here instead of later.
addStack();
/**/
