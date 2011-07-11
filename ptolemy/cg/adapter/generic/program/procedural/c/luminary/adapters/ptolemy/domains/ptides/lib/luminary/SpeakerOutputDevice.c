/***preinitBlock***/
#define MAX_SPEAKER_DEVICE_BUFFER 1000
static unsigned short $actorSymbol(result)[MAX_SPEAKER_DEVICE_BUFFER];
int bufferHead = 0;
int bufferTail = 0;
int bufferCount = 0;
static unsigned long g_sysClock;
static int previous = 0;
/**/

/*** sharedBlock ***/
//GPIOA_Transmitter sharedBlock
/**/

/*** initBlock ***/
//GPIOA_Transmitter initBlock
/**/

/*** initializeAudioOutput ***/
int i = 0;
for (i = 0; i < MAX_SPEAKER_DEVICE_BUFFER; i++) {
    $actorSymbol(result)[i] = SILENCE;
}

SysCtlPWMClockSet(SYSCTL_PWMDIV_8);
SysCtlPeripheralEnable(SYSCTL_PERIPH_PWM);
SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOG);
GPIOPinTypePWM(GPIO_PORTG_BASE, GPIO_PIN_1);

//
// Turn off the PWM generator 0 outputs.
//
PWMOutputState(PWM_BASE, PWM_OUT_0_BIT | PWM_OUT_1_BIT, false);
PWMGenDisable(PWM_BASE, PWM_GEN_0);

//
// Configure the PWM generator.  Up/down counting mode is used simply to
// gain an additional bit of range and resolution.
//
PWMGenConfigure(PWM_BASE, PWM_GEN_0,
                PWM_GEN_MODE_UP_DOWN | PWM_GEN_MODE_SYNC);
//
// from audioMute()
// Disable the PWM output.
//
PWMOutputState(PWM_BASE, PWM_OUT_1_BIT, false);
PWMOutputInvert(PWM_BASE, PWM_OUT_1_BIT, false);

//
// Set the PWM frequency to 40 KHz, beyond the range of human hearing.
//
g_sysClock = SysCtlClockGet();
PWMGenPeriodSet(PWM_BASE, PWM_GEN_0, SysCtlClockGet() / (SILENCE * 8));
PWMSyncUpdate(PWM_BASE, PWM_GEN_0_BIT);

//
// Enable the generator.
//
PWMGenEnable(PWM_BASE, PWM_GEN_0);

HWREG(PWM_BASE + PWM_GEN_0_OFFSET + PWM_O_X_CMPB) = 100;
PWMSyncUpdate(PWM_BASE, PWM_GEN_0_BIT);

//
// Turn on the output since it might have been muted previously.
//
PWMOutputState(PWM_BASE, PWM_OUT_1_BIT, true);
PWMOutputInvert(PWM_BASE, PWM_OUT_1_BIT, true);

/**/

/*** fireBlock($actuator) ***/
Time currentPhysicalTime;
if ($hasToken(input#0)) {
    $actorSymbol(result)[bufferTail++] = $get(input#0);
}
bufferCount++;
if (bufferTail == MAX_SPEAKER_DEVICE_BUFFER) {
    bufferTail = 0;
}

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

        setActuationInterrupt($actuator);

        #ifdef LCD_DEBUG
    //debugMessage("dead miss");
        #endif
}
/**/

/*** actuationBlock ***/
//
// Set the PWM frequency to the next frequency in the sound effect.
//
//Only Actuate if the previous was different than the current one
if (previous != $actorSymbol(result)[bufferHead]) {
    previous = $actorSymbol(result)[bufferHead];
    PWMGenPeriodSet(PWM_BASE, PWM_GEN_0, $actorSymbol(result)[bufferHead++]);
    PWMSyncUpdate(PWM_BASE, PWM_GEN_0_BIT);
} else {
    bufferHead++;
}
bufferCount--;
if (bufferHead == MAX_SPEAKER_DEVICE_BUFFER) {
    bufferHead = 0;
}
if (bufferHead == bufferTail && bufferCount != 0) {
    die("MAX_SPEAKER_DEVICE_BUFFER not big enough");
}

/**/
