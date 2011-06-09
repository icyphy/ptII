/*
 * File: dynamicsControll.h
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

#ifndef _RTW_HEADER_dynamicsControll_h_
#define _RTW_HEADER_dynamicsControll_h_

#include <float.h>
#include <string.h>
#include <math.h>
#include "../rtwtypes.h"
#include "../rtw_continuous.h"
#include "../rtw_solver.h"
#include "../rtlibsrc.h"

/* Child system includes */
#include "dynamicsController.h"

#include "dynamicsControll_types.h"

/* Macros for accessing real-time model data structure  */

#ifndef rtmGetErrorStatus
# define rtmGetErrorStatus(rtm) ((rtm)->errorStatus)
#endif

#ifndef rtmSetErrorStatus
# define rtmSetErrorStatus(rtm, val) ((rtm)->errorStatus = ((val)))
#endif

/* Block signals (auto storage) */
typedef struct _BlockIO_dynamicsControll {
  real_T grad2rad;                      /* '<S7>/grad2rad' */
} BlockIO_dynamicsControll;

/* Block states (auto storage) for system: '<Root>' */
typedef struct D_Work_dynamicsControll_tag {
  real_T DiscreteTransferFcn_DSTATE;    /* '<S9>/Discrete Transfer Fcn' */
} D_Work_dynamicsControll;

/* External inputs (root inport signals with auto storage) */
typedef struct _ExternalInputs_dynamicsControll_tag {
  real_T delta_f;                       /* '<Root>/delta_f[°]' */
  real_T vms;                           /* '<Root>/v[m//s]' */
  real_T yawd_acts;                     /* '<Root>/yawd_act[°//s]' */
  real_T delta_rrad;                    /* '<Root>/delta_r[rad]' */
} ExternalInputs_dynamicsControll;

/* External outputs (root outports fed by signals with auto storage) */
typedef struct _ExternalOutputs_dynamicsControll_tag {
  real_T delta_r_sprad;                 /* '<Root>/delta_r_sp[rad]' */
} ExternalOutputs_dynamicsControll;

/* Parameters (auto storage) */
struct _Parameters_dynamicsControll {
  real_T LUT_P_L_XData[4];              /* Expression: [40  50    100   200]
                                         * '<S9>/LUT_P_L'
                                         */
  real_T LUT_P_L_YData[4];              /* Expression: [0  0.04  0.04  0.04]
                                         * '<S9>/LUT_P_L'
                                         */
  real_T LUT_P_S_XData[4];              /* Expression: [40  50    100   200]
                                         * '<S9>/LUT_P_S'
                                         */
  real_T LUT_P_S_YData[4];              /* Expression: [0  0.004  0.004  0.004]
                                         * '<S9>/LUT_P_S'
                                         */
  real_T i_v_Gain;                      /* Expression: 1/19.2
                                         * '<S8>/1//i_v'
                                         */
  real_T grad2rad1_Gain;                /* Expression: pi/180
                                         * '<S8>/grad2rad1'
                                         */
  real_T v_ch2_Value;                   /* Expression: 25^2
                                         * '<S11>/v_ch2'
                                         */
  real_T one_Value;                     /* Expression: 1
                                         * '<S11>/one'
                                         */
  real_T length_Value;                  /* Expression: car.l_v+car.l_h
                                         * '<S11>/length'
                                         */
  real_T Gain2_Gain;                    /* Expression: 180/pi
                                         * '<S8>/Gain2'
                                         */
  real_T deg2rad1_Gain;                 /* Expression: pi/180
                                         * '<S9>/deg2rad1'
                                         */
  real_T deg2rad2_Gain;                 /* Expression: pi/180
                                         * '<S9>/deg2rad2'
                                         */
  real_T ms2kmh_Gain;                   /* Expression: 3.6
                                         * '<S7>/ms2kmh'
                                         */
  real_T rad2deg_Gain;                  /* Expression: 180/pi
                                         * '<S9>/rad2deg'
                                         */
  real_T deg2mm_Gain;                   /* Expression: 1/0.32
                                         * '<S9>/deg2mm'
                                         */
  real_T DiscreteTransferFcn_A;         /* Computed Parameter: A
                                         * '<S9>/Discrete Transfer Fcn'
                                         */
  real_T DiscreteTransferFcn_C;         /* Computed Parameter: C
                                         * '<S9>/Discrete Transfer Fcn'
                                         */
  real_T DiscreteTransferFcn_D;         /* Computed Parameter: D
                                         * '<S9>/Discrete Transfer Fcn'
                                         */
  real_T i_v_Gain_o;                    /* Expression: 1/19.2
                                         * '<S10>/1//i_v'
                                         */
  real_T u10_Gain;                      /* Expression: -1/10
                                         * '<S10>/-1//10'
                                         */
  real_T LUT_LRW_RED_XData[3];          /* Expression: [0 10 40]
                                         * '<S10>/LUT_LRW_RED'
                                         */
  real_T LUT_LRW_RED_YData[3];          /* Expression: [1 1 0]
                                         * '<S10>/LUT_LRW_RED'
                                         */
  real_T mm2deg_Gain;                   /* Expression: 0.32
                                         * '<S7>/mm2deg'
                                         */
  real_T grad2rad_Gain;                 /* Expression: pi/180
                                         * '<S7>/grad2rad'
                                         */
};

/* Real-time Model Data Structure */
struct _RT_MODEL_dynamicsControll_Tag {
  const char *errorStatus;
};

/* Block parameters (auto storage) */
extern Parameters_dynamicsControll dynamicsControll_P;

/* Block signals (auto storage) */
extern BlockIO_dynamicsControll dynamicsControll_B;

/* Block states (auto storage) */
extern D_Work_dynamicsControll dynamicsControll_DWork;

/* External inputs (root inport signals with auto storage) */
extern ExternalInputs_dynamicsControll dynamicsControll_U;

/* External outputs (root outports fed by signals with auto storage) */
extern ExternalOutputs_dynamicsControll dynamicsControll_Y;

/* Model entry point functions */

extern void dynamicsControll_initialize(boolean_T firstTime);
extern void dynamicsControll_step(void);
extern void dynamicsControll_terminate(void);

void dynamicsControll_updateInput(void);

/* Real-time Model object */
extern RT_MODEL_dynamicsControll *dynamicsControll_M;

/* 
 * The generated code includes comments that allow you to trace directly 
 * back to the appropriate location in the model.  The basic format
 * is <system>/block_name, where system is the system number (uniquely
 * assigned by Simulink) and block_name is the name of the block.
 *
 * Use the MATLAB hilite_system command to trace the generated code back
 * to the model.  For example,
 *
 * hilite_system('<S3>')    - opens system 3
 * hilite_system('<S3>/Kp') - opens and selects block Kp which resides in S3
 *
 * Here is the system hierarchy for this model
 *
 * '<Root>' : dynamicsControll
 * '<S1>'   : dynamicsControll/dynamicsController
 * '<S2>'   : dynamicsControll/xx_LockDownSignalSpec_1
 * '<S3>'   : dynamicsControll/xx_LockDownSignalSpec_2
 * '<S4>'   : dynamicsControll/xx_LockDownSignalSpec_3
 * '<S5>'   : dynamicsControll/xx_LockDownSignalSpec_4
 * '<S6>'   : dynamicsControll/xx_LockDownSignalSpec_5
 * '<S7>'   : dynamicsControll/dynamicsController/Regler
 * '<S8>'   : dynamicsControll/dynamicsController/sollwertberechnung
 * '<S9>'   : dynamicsControll/dynamicsController/Regler/P_Regler
 * '<S10>'  : dynamicsControll/dynamicsController/Regler/Vorsteuerung
 * '<S11>'  : dynamicsControll/dynamicsController/sollwertberechnung/ay_estimate
 */

#endif                                  /* _RTW_HEADER_dynamicsControll_h_ */

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
