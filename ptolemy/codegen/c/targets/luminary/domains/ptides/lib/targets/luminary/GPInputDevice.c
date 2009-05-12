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
$send(output#0, 1);
/**/
 
/*** sensingBlock($sensorFireMethod, $pad, $pin) ***/
GPIOPinIntClear(GPIO_PORT$pad_BASE, GPIO_PIN_0 | GPIO_PIN_1 | GPIO_PIN_2 | GPIO_PIN_3 | GPIO_PIN_4 | GPIO_PIN_5 | GPIO_PIN_6 | GPIO_PIN_7);
#ifdef LCD_DEBUG
    sprintf(str,"$pad");
    RIT128x96x4StringDraw(str, 115,0,15);
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
