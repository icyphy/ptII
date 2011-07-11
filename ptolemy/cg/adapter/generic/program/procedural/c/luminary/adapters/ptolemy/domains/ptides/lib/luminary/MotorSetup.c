/***preinitBlock***/
void pwmDisable(void){
        PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_FWD, 0);                                //0% duty cycle
        PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_REV, 0);
        PWMOutputState(PWM_BASE, MOTOR_PWM_FWD_BIT, 0);                                //disable channel
        PWMOutputState(PWM_BASE, MOTOR_PWM_REV_BIT, 0);
        PWMGenDisable(PWM_BASE, MOTOR_PWM);                                                        //disable PWM generator
}
#define NUM_OUTPUTS 10
int32 motorOutputPower[NUM_OUTPUTS];
uint32 motorNextOutput = 0;
uint32 motorLastOutput = 0;
uint32 numMotorOutputs = 0;
/**/

/*** sharedBlock ***/
#include "hw_types.h"
#include "pwm.h"
#define INTERRUPT_LATENCY                4                                        //Interrupt latency of the processor, in cycles (see LM3s8962 datasheet)

//Motor output
#define MOTOR_PWM                                PWM_GEN_1                        // Set PWM_GEN_1 (PWM2 and PWM3) as motor output
#define MOTOR_PWM_FWD                        PWM_OUT_2                        // Set PWM2 (PB0/PWM2) as motor forward output channel
#define MOTOR_PWM_FWD_BIT                PWM_OUT_2_BIT
#define MOTOR_PWM_REV                        PWM_OUT_3                        // Set PWM3 (PB1/PMW3) as motor reverse output channel
#define MOTOR_PWM_REV_BIT                PWM_OUT_3_BIT
#define MOTOR_PWM_PERIOD                50000                                // 50MHz clock / 50000 ticks = 1KHz PWM
#define MOTOR_MAX_SPEED                        (MOTOR_PWM_PERIOD - INTERRUPT_LATENCY)        // Largest PWM duty cycle, the range
                                                                                                                                                //        [PWM_PERIOD - INTERRUPT_LATENCY, PWM_PERIOD] results in an exception.
#define MOTOR_MIN_SPEED                        INTERRUPT_LATENCY                                                // Smallest nonzero PWM duty cycle, the range
                                                                                                                                                //        [1, INTERRUPT_LATENCY] results in an exception.
/**/

/*** initBlock ***/
/**/

/*** initializeActuationOutput ***/
        SysCtlPWMClockSet(SYSCTL_PWMDIV_1);
        SysCtlPeripheralEnable(SYSCTL_PERIPH_PWM);
        SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOB);
        GPIOPinTypePWM(GPIO_PORTB_BASE, GPIO_PIN_0 | GPIO_PIN_1);

        //Motor PWM generator
        PWMGenConfigure(PWM_BASE, MOTOR_PWM, PWM_GEN_MODE_UP_DOWN | PWM_GEN_MODE_NO_SYNC);
        PWMGenPeriodSet(PWM_BASE, MOTOR_PWM, MOTOR_PWM_PERIOD);

        //Motor foward PWM
        PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_FWD, 0);                                //begin with 0% duty cycle
        PWMOutputState(PWM_BASE, MOTOR_PWM_FWD_BIT, 1);                                //enable channel

        //Motor reverse PWM
        PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_REV, 0);                                //begin with 0% duty cycle
        PWMOutputState(PWM_BASE, MOTOR_PWM_REV_BIT, 1);                                //enable channel

        //Enable PWM generator
        PWMGenEnable(PWM_BASE, MOTOR_PWM);
/**/

/*** fireBlock($actuator) ***/
Time currentRealTime;
disableInterrupts();
if (numMotorOutputs == NUM_OUTPUTS) {
        die("motor output size too small");
}
numMotorOutputs++;
motorOutputPower[motorLastOutput++] = $get(input#0);
if (motorLastOutput >= NUM_OUTPUTS) {
    motorLastOutput = 0;
}
enableInterrupts();
getRealTime(&currentRealTime);
if (timeCompare(currentRealTime, currentModelTime) <= 0) {
        setActuationInterrupt($actuator);
} else {
        die("dead miss");
}
//if ($get(input#0) != 0) {
//        debugSendPacket(&saved_discDesired, &saved_g_disc, saved_timeLeft, saved_controlSignal, saved_tempTime);
//}
/**/

/*** actuationBlock ***/
// power is a 32bit int, which is the input to this actor.
uint32 pwmGo;                        //pwm channel to enable
uint32 pwmStop;                        //pwm channel to disable
int32 power;
disableInterrupts();
if (numMotorOutputs == 0) {
        die("trying to actuate but no power available");
}
numMotorOutputs--;
power = motorOutputPower[motorNextOutput++];
if (motorNextOutput >= NUM_OUTPUTS) {
    motorNextOutput = 0;
}
enableInterrupts();

//Assign PWM channel to enable (either forward or reverse)
if(power >= 0){
        pwmGo = MOTOR_PWM_FWD;
        pwmStop = MOTOR_PWM_REV;
}
else{
        pwmGo = MOTOR_PWM_REV;
        pwmStop = MOTOR_PWM_FWD;

        power = -power;
}

//Velocities between 0 and MOTOR_MIN_SPEED cause PWM errors
if((power != 0) && (power < MOTOR_MIN_SPEED))
        power = MOTOR_MIN_SPEED;
else if(power > MOTOR_MAX_SPEED)
        power = MOTOR_MAX_SPEED;

//Disable opposing direction channel
PWMPulseWidthSet(PWM_BASE, pwmStop, 0);

//Enable desired direction channel
PWMPulseWidthSet(PWM_BASE, pwmGo, power);
/**/
