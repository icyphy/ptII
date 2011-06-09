/*
 * File: carModel.c
 *
 * Real-Time Workshop code generated for Simulink model carModel.
 *
 * Model version                        : 1.0
 * Real-Time Workshop file version      : 6.0  (R14)  05-May-2004
 * Real-Time Workshop file generated on : Tue Jan 20 15:04:51 2009
 * TLC version                          : 6.0 (Apr 27 2004)
 * C source code generated on           : Tue Jan 20 15:04:51 2009
 *
 * You can customize this banner by specifying a different template.
 */

#include "carModel.h"
#include "carModel_private.h" 

/* Block states (auto storage) */
D_Work_carModel carModel_DWork;

/* External inputs (root inport signals with auto storage) */
ExternalInputs_carModel carModel_U;

/* External output (root outports fed by signals with auto storage) */
ExternalOutputs_carModel carModel_Y;

/* Real-time model */
RT_MODEL_carModel carModel_M_;
RT_MODEL_carModel *carModel_M = &carModel_M_;


/* Model step function */
void carModel_step()
{

  /* local block i/o variables */

  real_T rtb_grad2rad1;
  real_T rtb_UnitDelay_i;
  real_T rtb_voltageconverter;
  real_T rtb_motorelectrical;
  real_T rtb_Sum1;
  real_T rtb_motormechanical;
  real_T rtb_phi;
  real_T rtb_Internal[2];
  real_T rtb_temp12;

  /* Gain: '<S9>/grad2rad1' incorporates:
   *  Gain: '<S9>/1//i_v'
   */
/*  rtb_grad2rad1 = carModel_U.delta_f * carModel_P.i_v_Gain *
    carModel_P.grad2rad1_Gain;
*/

/* carModel_U.delta_f connected to one model input */
  rtb_grad2rad1 = front_angle * carModel_P.i_v_Gain *
    carModel_P.grad2rad1_Gain;

  /* UnitDelay: '<S1>/Unit Delay' */
  rtb_UnitDelay_i = carModel_DWork.UnitDelay_DSTATE_l;

  /* DiscreteTransferFcn: '<S8>/voltage converter' */
  rtb_voltageconverter = carModel_P.voltageconverter_D*rtb_UnitDelay_i;
  rtb_voltageconverter +=
    carModel_P.voltageconverter_C*carModel_DWork.voltageconverter_DSTATE;

  /* DiscreteTransferFcn: '<S8>/motor electrical' */
  rtb_motorelectrical = carModel_P.motorelectrical_D*rtb_voltageconverter;
  rtb_motorelectrical +=
    carModel_P.motorelectrical_C*carModel_DWork.motorelectrical_DSTATE;

  /* Sum: '<S8>/Sum1' */
  rtb_Sum1 = rtb_motorelectrical - (carModel_DWork.UnitDelay_DSTATE *
    carModel_P.Mr_Gain);

  /* DiscreteTransferFcn: '<S8>/motor mechanical' */
  rtb_motormechanical = carModel_P.motormechanical_D*rtb_Sum1;
  rtb_motormechanical +=
    carModel_P.motormechanical_C*carModel_DWork.motormechanical_DSTATE;

  /* DiscreteTransferFcn: '<S8>/phi' */
  rtb_phi = carModel_P.phi_D*rtb_motormechanical;
  rtb_phi += carModel_P.phi_C*carModel_DWork.phi_DSTATE;

  /* DiscreteStateSpace: '<S10>/Internal' incorporates:
   *  Constant: '<S1>/Constant'
   */
  {
    rtb_Internal[0] = (carModel_P.Internal_D[0])*rtb_grad2rad1 +
      (carModel_P.Internal_D[1])*rtb_phi
      + (carModel_P.Internal_D[2])*carModel_P.Constant_Value;
    rtb_Internal[0] +=
      (carModel_P.Internal_C[0])*carModel_DWork.Internal_DSTATE[1];

    rtb_Internal[1] = (carModel_P.Internal_D[3])*rtb_grad2rad1 +
      (carModel_P.Internal_D[4])*rtb_phi
      + (carModel_P.Internal_D[5])*carModel_P.Constant_Value;
    rtb_Internal[1] +=
      (carModel_P.Internal_C[1])*carModel_DWork.Internal_DSTATE[3];
  }

  /* Outport: '<Root>/yaw rate' incorporates:
   *  Gain: '<S9>/rad2grad'
   */
  carModel_Y.yawrate = rtb_Internal[1] * carModel_P.rad2grad_Gain;

  /* Outport: '<Root>/rear angle' */
  carModel_Y.rearangle = rtb_phi; 

  /* DiscreteTransferFcn: '<S8>/speed sensor' */
  rtb_temp12 = carModel_P.speedsensor_D*rtb_motormechanical;
  rtb_temp12 += carModel_P.speedsensor_C*carModel_DWork.speedsensor_DSTATE;

  /* Outport: '<Root>/angle rate' */
  carModel_Y.anglerate = rtb_temp12;

  /* DiscreteTransferFcn: '<S8>/phi1' */
  rtb_temp12 = carModel_P.phi1_D*rtb_motorelectrical;
  rtb_temp12 += carModel_P.phi1_C*carModel_DWork.phi1_DSTATE;

  /* Outport: '<Root>/motor current' */
  carModel_Y.motorcurrent = rtb_temp12;

  /* Update for UnitDelay: '<S8>/Unit Delay' */
  carModel_DWork.UnitDelay_DSTATE = rtb_motormechanical;

  /* Update for UnitDelay: '<S1>/Unit Delay' */
//carModel_DWork.UnitDelay_DSTATE_l = carModel_U.U;
carModel_DWork.UnitDelay_DSTATE_l = voltage;

  /* DiscreteTransferFcn Block: <S8>/voltage converter */
  {
    carModel_DWork.voltageconverter_DSTATE = rtb_UnitDelay_i +
      carModel_P.voltageconverter_A*carModel_DWork.voltageconverter_DSTATE;
  }

  /* DiscreteTransferFcn Block: <S8>/motor electrical */
  {
    carModel_DWork.motorelectrical_DSTATE = rtb_voltageconverter +
      carModel_P.motorelectrical_A*carModel_DWork.motorelectrical_DSTATE;
  }

  /* DiscreteTransferFcn Block: <S8>/motor mechanical */
  {
    carModel_DWork.motormechanical_DSTATE = rtb_Sum1 +
      carModel_P.motormechanical_A*carModel_DWork.motormechanical_DSTATE;
  }

  /* DiscreteTransferFcn Block: <S8>/phi */
  {
    carModel_DWork.phi_DSTATE = rtb_motormechanical +
      carModel_P.phi_A*carModel_DWork.phi_DSTATE;
  }

  /* DiscreteStateSpace Block: <S10>/Internal */
  {
    static real_T xnew[4];
    xnew[0] = (carModel_P.Internal_B[0])*rtb_grad2rad1 +
      (carModel_P.Internal_B[1])*rtb_phi
      + (carModel_P.Internal_B[2])*carModel_P.Constant_Value;
    xnew[0] += (carModel_P.Internal_A[0])*carModel_DWork.Internal_DSTATE[1];

    xnew[1] = (carModel_P.Internal_B[3])*rtb_grad2rad1 +
      (carModel_P.Internal_B[4])*rtb_phi
      + (carModel_P.Internal_B[5])*carModel_P.Constant_Value;
    xnew[1] += (carModel_P.Internal_A[1])*carModel_DWork.Internal_DSTATE[0] +
      (carModel_P.Internal_A[2])*carModel_DWork.Internal_DSTATE[1];

    xnew[2] = (carModel_P.Internal_B[6])*rtb_grad2rad1 +
      (carModel_P.Internal_B[7])*rtb_phi
      + (carModel_P.Internal_B[8])*carModel_P.Constant_Value;
    xnew[2] += (carModel_P.Internal_A[3])*carModel_DWork.Internal_DSTATE[3];

    xnew[3] = (carModel_P.Internal_B[9])*rtb_grad2rad1 +
      (carModel_P.Internal_B[10])*rtb_phi
      + (carModel_P.Internal_B[11])*carModel_P.Constant_Value;
    xnew[3] += (carModel_P.Internal_A[4])*carModel_DWork.Internal_DSTATE[2] +
      (carModel_P.Internal_A[5])*carModel_DWork.Internal_DSTATE[3];
    (void)memcpy(&carModel_DWork.Internal_DSTATE[0], xnew, sizeof(real_T)*4);
  }

  /* DiscreteTransferFcn Block: <S8>/speed sensor */
  {
    carModel_DWork.speedsensor_DSTATE = rtb_motormechanical +
      carModel_P.speedsensor_A*carModel_DWork.speedsensor_DSTATE;
  }

  /* DiscreteTransferFcn Block: <S8>/phi1 */
  {
    carModel_DWork.phi1_DSTATE = rtb_motorelectrical +
      carModel_P.phi1_A*carModel_DWork.phi1_DSTATE;
  }
}

/* Model initialize function */
void carModel_initialize(boolean firstTime)
{

  if (firstTime) {
    /* registration code */

    /* initialize error status */
    rtmSetErrorStatus(carModel_M, (const char_T *)0);

    /* data type work */
    {
      int_T i;
      real_T *dwork_ptr = (real_T *) &carModel_DWork.UnitDelay_DSTATE;

      for (i = 0; i < 12; i++) {
        dwork_ptr[i] = 0.0;
      }
    }

    /* external inputs */
    carModel_U.delta_f = 0.0;
    carModel_U.U = 0.0;

    /* external outputs */
    carModel_Y.yawrate = 0.0;
    carModel_Y.rearangle = 0.0;
    carModel_Y.anglerate = 0.0;
    carModel_Y.motorcurrent = 0.0;
  }

  /* InitializeConditions for UnitDelay: '<S8>/Unit Delay' */
  carModel_DWork.UnitDelay_DSTATE = carModel_P.UnitDelay_X0;

  /* InitializeConditions for UnitDelay: '<S1>/Unit Delay' */
  carModel_DWork.UnitDelay_DSTATE_l = carModel_P.UnitDelay_X0_d;

  /* DiscreteStateSpace Block: <S10>/Internal */
  carModel_DWork.Internal_DSTATE[0] = 0.0;
  carModel_DWork.Internal_DSTATE[1] = 0.0;
  carModel_DWork.Internal_DSTATE[2] = 0.0;
  carModel_DWork.Internal_DSTATE[3] = 0.0;
}


/* Model terminate function */
void carModel_terminate(void)
{
  /* (no terminate code required) */
}

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
