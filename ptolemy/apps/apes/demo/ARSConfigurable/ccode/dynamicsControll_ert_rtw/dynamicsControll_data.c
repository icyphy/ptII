/*
 * File: dynamicsControll_data.c
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

#include "dynamicsControll.h"
#include "dynamicsControll_private.h"

/* Block parameters (auto storage) */
Parameters_dynamicsControll dynamicsControll_P = {
  /*  LUT_P_L_XData : '<S9>/LUT_P_L' */
  { 40.0, 50.0, 100.0, 200.0 } ,
  /*  LUT_P_L_YData : '<S9>/LUT_P_L' */
  { 0.0, 0.04, 0.04, 0.04 } ,
  /*  LUT_P_S_XData : '<S9>/LUT_P_S' */
  { 40.0, 50.0, 100.0, 200.0 } ,
  /*  LUT_P_S_YData : '<S9>/LUT_P_S' */
  { 0.0, 0.004, 0.004, 0.004 } ,
  5.2083333333333336E-002 ,             /* i_v_Gain : '<S8>/1//i_v' */
  1.7453292519943295E-002 ,             /* grad2rad1_Gain : '<S8>/grad2rad1' */
  625.0 ,                               /* v_ch2_Value : '<S11>/v_ch2' */
  1.0 ,                                 /* one_Value : '<S11>/one' */
  2.57 ,                                /* length_Value : '<S11>/length' */
  5.7295779513082323E+001 ,             /* Gain2_Gain : '<S8>/Gain2' */
  1.7453292519943295E-002 ,             /* deg2rad1_Gain : '<S9>/deg2rad1' */
  1.7453292519943295E-002 ,             /* deg2rad2_Gain : '<S9>/deg2rad2' */
  3.6 ,                                 /* ms2kmh_Gain : '<S7>/ms2kmh' */
  5.7295779513082323E+001 ,             /* rad2deg_Gain : '<S9>/rad2deg' */
  3.125 ,                               /* deg2mm_Gain : '<S9>/deg2mm' */
  1.0 ,                                 /* DiscreteTransferFcn_A : '<S9>/Discrete Transfer Fcn' */
  0.1 ,                                 /* DiscreteTransferFcn_C : '<S9>/Discrete Transfer Fcn' */
  0.05 ,                                /* DiscreteTransferFcn_D : '<S9>/Discrete Transfer Fcn' */
  5.2083333333333336E-002 ,             /* i_v_Gain_o : '<S10>/1//i_v' */
  -0.1 ,                                /* u10_Gain : '<S10>/-1//10' */
  /*  LUT_LRW_RED_XData : '<S10>/LUT_LRW_RED' */
  { 0.0, 10.0, 40.0 } ,
  /*  LUT_LRW_RED_YData : '<S10>/LUT_LRW_RED' */
  { 1.0, 1.0, 0.0 } ,
  0.32 ,                                /* mm2deg_Gain : '<S7>/mm2deg' */
  1.7453292519943295E-002               /* grad2rad_Gain : '<S7>/grad2rad' */
};

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
