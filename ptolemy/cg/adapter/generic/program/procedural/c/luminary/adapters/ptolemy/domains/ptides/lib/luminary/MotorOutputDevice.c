/***preinitBlock***/
#define INTERRUPT_LATENCY		4					//Interrupt latency of the processor, in cycles (see LM3s8962 datasheet)

//Motor output
#define MOTOR_PWM				PWM_GEN_1			// Set PWM_GEN_1 (PWM2 and PWM3) as motor output
#define MOTOR_PWM_FWD			PWM_OUT_2			// Set PWM2 (PB0/PWM2) as motor forward output channel
#define MOTOR_PWM_FWD_BIT		PWM_OUT_2_BIT
#define MOTOR_PWM_REV			PWM_OUT_3			// Set PWM3 (PB1/PMW3) as motor reverse output channel
#define MOTOR_PWM_REV_BIT		PWM_OUT_3_BIT
#define MOTOR_PWM_PERIOD		50000				// 50MHz clock / 50000 ticks = 1KHz PWM
#define MOTOR_MAX_SPEED			(MOTOR_PWM_PERIOD - INTERRUPT_LATENCY)	// Largest PWM duty cycle, the range
																		//	[PWM_PERIOD - INTERRUPT_LATENCY, PWM_PERIOD] results in an exception.
#define MOTOR_MIN_SPEED			INTERRUPT_LATENCY						// Smallest nonzero PWM duty cycle, the range
																		//	[1, INTERRUPT_LATENCY] results in an exception.
#define MOTOR_SPIN_DELAY		10000				// Spinup sampling period in us (used for testing motor)

void pwmDisable(void){
	PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_FWD, 0);				//0% duty cycle
	PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_REV, 0);
	PWMOutputState(PWM_BASE, MOTOR_PWM_FWD_BIT, 0);				//disable channel
	PWMOutputState(PWM_BASE, MOTOR_PWM_REV_BIT, 0);
	PWMGenDisable(PWM_BASE, MOTOR_PWM);							//disable PWM generator
}

/**/

/*** sharedBlock ***/
#include "hw_types.h"
#include "hw_ints.h"
#include "hw_memmap.h"
#include "sysctl.h"
#include "interrupt.h"
#include "gpio.h"
#include "pwm.h"

//Number of encoder pulses per revolution of the disc; this takes into account gearing ratio
#define ENCODER_TICKS_PER_REV	1000

#ifndef _UTILITY_H
#define _UTILITY_H

#define sign(x)					((x) < 0 ? -1 : 1)			//Signum function
#define ABS(x) 					((x) > 0 ? (x) : -(x))		//Absolute value
#define MIN(x, y)				((x) < (y) ? (x) : (y) )	//Least of two numbers
#define MAX(x, y)				((x) > (y) ? (x) : (y) )	//Greatest of two numbers
#define coerce(min, x, max) 	((x) < (min) ? (min) : ((x) > (max) ? (max) : (x)))	//Coerce a number to fall within a range

//Explicit type definitions for the Luminary LM3s8962
typedef unsigned long long      uint64;
typedef unsigned long           uint32;
typedef unsigned int            uint16;
typedef unsigned char           uint8;
typedef uint8                   byte;
typedef signed long long        int64;
typedef signed long             int32;
typedef signed int              int16;
typedef signed char             int8;

//Byte-level operations
#define LO(x)					((x) & 0xFF)			//Low order byte of a 16 bit number
#define HO(x)					((x) >> 0x08)			//High order byte of a 16 bit number
#define LLO(x)					((x) & 0xFF)			//Byte 0 (lowest order) byte of a 32-bit number
#define LHO(x)					(((x) >> 0x08) & 0xFF)	//Byte 1 of a 32-bit number
#define HLO(x)					(((x) >> 0x10) & 0xFF)	//Byte 2 of a 32-bit number
#define HHO(x)					((x) >> 0x18)			//Byte 3 (highest order) byte of a 32-bit number

//Dics position and period object captures the state of the disc
typedef struct{
	int32	position;		//Absolute position of the disc, in encoder ticks
	int32	period;			//Period between encoder ticks
} Disc;

#define DISC_SMALLEST_RATE		(~(1 << 30))	// Smallest rate (closest to zero) corresponds to the largest encoder period
extern const Disc Disc_0;						// Used as a reasonable initial value for Disc variables
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
	PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_FWD, 0);				//begin with 0% duty cycle
	PWMOutputState(PWM_BASE, MOTOR_PWM_FWD_BIT, 1);				//enable channel
	
	//Motor reverse PWM
	PWMPulseWidthSet(PWM_BASE, MOTOR_PWM_REV, 0);				//begin with 0% duty cycle
	PWMOutputState(PWM_BASE, MOTOR_PWM_REV_BIT, 1);				//enable channel
	
	//Enable PWM generator
	PWMGenEnable(PWM_BASE, MOTOR_PWM);
/**/

/*** fireBlock($actuator) ***/
static int32 power;
power = $get(input#0);
setActuationInterrupt($actuator);
/**/
 
/*** actuationBlock ***/
// power is a 32bit int, which is the input to this actor.
uint32 pwmGo;			//pwm channel to enable
uint32 pwmStop;			//pwm channel to disable

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
