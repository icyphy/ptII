/*
 * File: motorController.c
 *
 * Real-Time Workshop code generated for Simulink model motorController.
 *
 * Model version                        : 1.0
 * Real-Time Workshop file version      : 6.0  (R14)  05-May-2004
 * Real-Time Workshop file generated on : Tue Jan 20 14:56:54 2009
 * TLC version                          : 6.0 (Apr 27 2004)
 * C source code generated on           : Tue Jan 20 14:56:54 2009
 *
 * You can customize this banner by specifying a different template.
 */
#include <stdio.h>
#include <stdlib.h> 
#include "motorController.h"
#include "motorController_private.h" 
#include "../dynamicsControll_ert_rtw/dynamicsControll.h"
/* Block states (auto storage) */
D_Work_motorController motorController_DWork;

/* External inputs (root inport signals with auto storage) */
ExternalInputs_motorController motorController_U;

/* External output (root outports fed by signals with auto storage) */
ExternalOutputs_motorController motorController_Y;

/* Real-time model */
RT_MODEL_motorController motorController_M_;
RT_MODEL_motorController *motorController_M = &motorController_M_;

extern double anglerate, rearangle, motorcurrent;

/* Model step function */
void motorController_step(void)
{

  /* local block i/o variables */

  real_T rtb_Sum2;
  real_T rtb_Sum;
  real_T rtb_temp4;

  /* Sum: '<S1>/Sum2' incorporates:
   *  Sum: '<S1>/Sum4'
   *  Gain: '<S1>/Kphi'
   */
/*  rtb_Sum2 = (motorController_U.delta_r_sp - motorController_U.delta_r_act) *
    motorController_P.Kphi_Gain - motorController_U.angular_rate;
*/ 
	
	callbackI(0.01, 0.0, "rearangle");  
	double rearangle_tmp = rearangle; 
	callbackI(0.01, 0.0, "anglerate"); 
	rtb_Sum2 = (dynamicsControll_Y.delta_r_sprad - rearangle_tmp) *
    motorController_P.Kphi_Gain - anglerate;

  /* DiscreteTransferFcn: '<S1>/Speed Reg' */
  rtb_temp4 = motorController_P.SpeedReg_D*rtb_Sum2;
  rtb_temp4 +=
    motorController_P.SpeedReg_C*motorController_DWork.SpeedReg_DSTATE;

  /* Sum: '<S1>/Sum' */
//  rtb_Sum = rtb_temp4 - motorController_U.current;

	callbackI(0.01, 0.0, "motorcurrent"); 
  rtb_Sum = rtb_temp4 - motorcurrent;

  /* DiscreteTransferFcn: '<S1>/Current Reg' */
  rtb_temp4 = motorController_P.CurrentReg_D*rtb_Sum;
  rtb_temp4 +=
    motorController_P.CurrentReg_C*motorController_DWork.CurrentReg_DSTATE;

  /* Outport: '<Root>/voltage' */
  callbackO(0.01, 0.0, "voltage", rtb_temp4);
  motorController_Y.voltage = rtb_temp4;



  /* DiscreteTransferFcn Block: <S1>/Speed Reg */
  {
    motorController_DWork.SpeedReg_DSTATE = rtb_Sum2 +
      motorController_P.SpeedReg_A*motorController_DWork.SpeedReg_DSTATE;
  }

  /* DiscreteTransferFcn Block: <S1>/Current Reg */
  {
    motorController_DWork.CurrentReg_DSTATE = rtb_Sum +
      motorController_P.CurrentReg_A*motorController_DWork.CurrentReg_DSTATE;
  }
}

/* Model initialize function */
void motorController_initialize(boolean_T firstTime)
{

  if (firstTime) {
    /* registration code */

    /* initialize error status */
    rtmSetErrorStatus(motorController_M, (const char_T *)0);

    /* data type work */
    {
      int_T i;
      real_T *dwork_ptr = (real_T *) &motorController_DWork.SpeedReg_DSTATE;

      for (i = 0; i < 2; i++) {
        dwork_ptr[i] = 0.0;
      }
    }

    /* external inputs */
    motorController_U.delta_r_sp = 0.0;
    motorController_U.delta_r_act = 0.0;
    motorController_U.angular_rate = 0.0;
    motorController_U.current = 0.0;

    /* external outputs */
    motorController_Y.voltage = 0.0;
  }
}

/* Model terminate function */
void motorController_terminate(void)
{
  /* (no terminate code required) */
}

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
