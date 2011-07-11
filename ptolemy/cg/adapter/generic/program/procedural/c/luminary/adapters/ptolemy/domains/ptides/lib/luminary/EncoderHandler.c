/***preinitBlock***/
int32 encoderInputInterruptStatus;
/**/

/*** sharedBlock ***/
#define ENCODER_PERIF                        SYSCTL_PERIPH_GPIOB
#define ENCODER_INT                                INT_GPIOB
#define ENCODER_BASE                         GPIO_PORTB_BASE
#define ENCODER_PIN_A                        GPIO_PIN_4                        // Set B[4] (PB4/C0+) as encoder input (encoder channel A)
#define ENCODER_PIN_B                        GPIO_PIN_6                        // Set B[6] (PB6/C0-) as encoder input (encoder channel B)
#define ENCODER_PIN_I                        GPIO_PIN_2                        // Set B[2] (PB2/SCL) as encoder input (encoder channel I)

/**/

/*** initBlock ***/
/**/

/*** fireBlock ***/
$put(output, encoderInputInterruptStatus);
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
encoderInputInterruptStatus = GPIOPinIntStatus(ENCODER_BASE, 0);
encoderInputInterruptStatus |= (GPIOPinRead(ENCODER_BASE, ENCODER_PIN_B) << 8);
/**/
