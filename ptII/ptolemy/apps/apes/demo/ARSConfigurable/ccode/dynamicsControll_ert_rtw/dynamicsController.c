/*
 * File: dynamicsController.c
 *
 * Real-Time Workshop code generated for Simulink model dynamicsControll.
 *
 * Model version                        : 1.0
 * Real-Time Workshop file version      : 6.0  (R14)  05-May-2004
 * Real-Time Workshop file generated on : Tue Jan 20 14:52:04 2009
 * TLC version                          : 6.0 (Apr 27 2004)
 * C source code generated on           : Tue Jan 20 14:52:05 2009
 *
 * You can customize this banner by specifying a different template.
 */

#include "dynamicsController.h"

/* Include model header file for global data */
#include "dynamicsControll.h"

#include "dynamicsControll_private.h"

/* Output and update for atomic system: '<Root>/dynamicsController' */
void dynamicsController(void)
{

  /* local block i/o variables */

  real_T rtb_deg2rad1;
  real_T rtb_deg2rad2;
  real_T rtb_ms2kmh;
  real_T rtb_P_ANTEIL;
  real_T rtb_deg2mm;
  real_T rtb_DiscreteTransferFcn;
  real_T rtb_LUT_LRW_RED;
  real_T rtb_LUT_P_S;
  real_T rtb_LUT_P_L;

  /* Gain: '<S9>/deg2rad1' incorporates:
   *  Gain: '<S8>/1//i_v'
   *  Gain: '<S8>/grad2rad1'
   *  Sum: '<S11>/Sum1'
   *  Product: '<S11>/Product'
   *  Sum: '<S11>/Sum'
   *  Product: '<S11>/Product1'
   *  Product: '<S11>/Product2'
   *  Gain: '<S8>/Gain2'
   */

  rtb_deg2rad1 = (dynamicsControll_U.delta_f * dynamicsControll_P.i_v_Gain *
    dynamicsControll_P.grad2rad1_Gain - dynamicsControll_U.delta_rrad) *
    dynamicsControll_U.vms / ((dynamicsControll_U.vms * dynamicsControll_U.vms /
    dynamicsControll_P.v_ch2_Value + dynamicsControll_P.one_Value) *
    dynamicsControll_P.length_Value) * dynamicsControll_P.Gain2_Gain *
    dynamicsControll_P.deg2rad1_Gain;

  /* Gain: '<S9>/deg2rad2' */
  rtb_deg2rad2 = dynamicsControll_U.yawd_acts * dynamicsControll_P.deg2rad2_Gain;

  /* Gain: '<S7>/ms2kmh' */
  rtb_ms2kmh = dynamicsControll_U.vms * dynamicsControll_P.ms2kmh_Gain;

  /* Switch: '<S9>/Switch' incorporates:
   *  Abs: '<S9>/Abs'
   *  Abs: '<S9>/Abs1'
   *  RelationalOperator: '<S9>/Greater Than'
   */
  if(fabs(rtb_deg2rad1) > fabs(rtb_deg2rad2)) {

    /* Lookup Block: '<S9>/LUT_P_S'
     * Input0  Data Type:  Floating Point real_T
     * Output0 Data Type:  Floating Point real_T
     * Round Mode: Nearest
     * Lookup Method: Linear_Endpoint
     *
     * XData parameter uses the same data type and scaling as Input0
     * YData parameter uses the same data type and scaling as Output0
     */
    LookUp_real_T_real_T_NEAR( &(rtb_LUT_P_S), dynamicsControll_P.LUT_P_S_YData,
     rtb_ms2kmh, dynamicsControll_P.LUT_P_S_XData, 3);

    rtb_P_ANTEIL = rtb_LUT_P_S;
  } else {

    /* Lookup Block: '<S9>/LUT_P_L'
     * Input0  Data Type:  Floating Point real_T
     * Output0 Data Type:  Floating Point real_T
     * Round Mode: Nearest
     * Lookup Method: Linear_Endpoint
     *
     * XData parameter uses the same data type and scaling as Input0
     * YData parameter uses the same data type and scaling as Output0
     */
    LookUp_real_T_real_T_NEAR( &(rtb_LUT_P_L), dynamicsControll_P.LUT_P_L_YData,
     rtb_ms2kmh, dynamicsControll_P.LUT_P_L_XData, 3);

    rtb_P_ANTEIL = rtb_LUT_P_L;
  }

  /* Gain: '<S9>/deg2mm' incorporates:
   *  Sum: '<S9>/Sum'
   *  Product: '<S9>/Product'
   *  Gain: '<S9>/rad2deg'
   */
  rtb_deg2mm = rtb_P_ANTEIL * (rtb_deg2rad2 - rtb_deg2rad1) *
    dynamicsControll_P.rad2deg_Gain * dynamicsControll_P.deg2mm_Gain;

  /* DiscreteTransferFcn: '<S9>/Discrete Transfer Fcn' */
  rtb_DiscreteTransferFcn = dynamicsControll_P.DiscreteTransferFcn_D*rtb_deg2mm;
  rtb_DiscreteTransferFcn +=
    dynamicsControll_P.DiscreteTransferFcn_C*dynamicsControll_DWork.DiscreteTransferFcn_DSTATE;

  /* Lookup Block: '<S10>/LUT_LRW_RED'
   * Input0  Data Type:  Floating Point real_T
   * Output0 Data Type:  Floating Point real_T
   * Round Mode: Nearest
   * Saturation Mode: Saturate
   * Lookup Method: Linear_Endpoint
   *
   * XData parameter uses the same data type and scaling as Input0
   * YData parameter uses the same data type and scaling as Output0
   */
  LookUp_real_T_real_T_SAT_NEAR( &(rtb_LUT_LRW_RED),
   dynamicsControll_P.LUT_LRW_RED_YData, rtb_ms2kmh,
   dynamicsControll_P.LUT_LRW_RED_XData, 2);

  /* Gain: '<S7>/grad2rad' incorporates:
   *  Product: '<S10>/Product1'
   *  Product: '<S10>/Product2'
   *  Sum: '<S7>/Sum'
   *  Gain: '<S7>/mm2deg'
   */
  dynamicsControll_B.grad2rad = (rtb_DiscreteTransferFcn +
    (rt_SGN(dynamicsControll_U.delta_f)) * ((fabs(dynamicsControll_U.delta_f) *
    dynamicsControll_P.i_v_Gain_o * dynamicsControll_P.u10_Gain) *
    rtb_LUT_LRW_RED)) * dynamicsControll_P.mm2deg_Gain *
    dynamicsControll_P.grad2rad_Gain;

  /* DiscreteTransferFcn Block: <S9>/Discrete Transfer Fcn */
  {
    dynamicsControll_DWork.DiscreteTransferFcn_DSTATE = rtb_deg2mm +
      dynamicsControll_P.DiscreteTransferFcn_A*dynamicsControll_DWork.DiscreteTransferFcn_DSTATE;
  }
}

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
