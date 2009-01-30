/*
 * File: carModel.h
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

#ifndef _RTW_HEADER_carModel_h_
#define _RTW_HEADER_carModel_h_

#include <math.h>
#include <float.h>
#include <string.h>
#include "../rtwtypes.h"
#include "../rtw_continuous.h"
#include "../rtw_solver.h"
#include "../rtlibsrc.h"

#include "carModel_types.h"

/* Macros for accessing real-time model data structure  */

#ifndef rtmGetErrorStatus
# define rtmGetErrorStatus(rtm) ((rtm)->errorStatus)
#endif

#ifndef rtmSetErrorStatus
# define rtmSetErrorStatus(rtm, val) ((rtm)->errorStatus = ((val)))
#endif

/* Block states (auto storage) for system: '<Root>' */
typedef struct D_Work_carModel_tag {
  real_T UnitDelay_DSTATE;              /* '<S8>/Unit Delay' */
  real_T UnitDelay_DSTATE_l;            /* '<S1>/Unit Delay' */
  real_T voltageconverter_DSTATE;       /* '<S8>/voltage converter' */
  real_T motorelectrical_DSTATE;        /* '<S8>/motor electrical' */
  real_T motormechanical_DSTATE;        /* '<S8>/motor mechanical' */
  real_T phi_DSTATE;                    /* '<S8>/phi' */
  real_T Internal_DSTATE[4];            /* '<S10>/Internal' */
  real_T speedsensor_DSTATE;            /* '<S8>/speed sensor' */
  real_T phi1_DSTATE;                   /* '<S8>/phi1' */
} D_Work_carModel;

/* External inputs (root inport signals with auto storage) */
typedef struct _ExternalInputs_carModel_tag {
  real_T delta_f;                       /* '<Root>/delta_f' */
  real_T U;                             /* '<Root>/U' */
} ExternalInputs_carModel;

/* External outputs (root outports fed by signals with auto storage) */
typedef struct _ExternalOutputs_carModel_tag {
  real_T yawrate;                       /* '<Root>/yaw rate' */
  real_T rearangle;                     /* '<Root>/rear angle' */
  real_T anglerate;                     /* '<Root>/angle rate' */
  real_T motorcurrent;                  /* '<Root>/motor current' */
} ExternalOutputs_carModel;

/* Parameters (auto storage) */
struct _Parameters_carModel {
  real_T i_v_Gain;                      /* Expression: 1/19.2
                                         * '<S9>/1//i_v'
                                         */
  real_T grad2rad1_Gain;                /* Expression: pi/180
                                         * '<S9>/grad2rad1'
                                         */
  real_T UnitDelay_X0;                  /* Expression: 0
                                         * '<S8>/Unit Delay'
                                         */
  real_T Mr_Gain;                       /* Expression: K
                                         * '<S8>/Mr'
                                         */
  real_T UnitDelay_X0_d;                /* Expression: 0
                                         * '<S1>/Unit Delay'
                                         */
  real_T voltageconverter_A;            /* Computed Parameter: A
                                         * '<S8>/voltage converter'
                                         */
  real_T voltageconverter_C;            /* Computed Parameter: C
                                         * '<S8>/voltage converter'
                                         */
  real_T voltageconverter_D;            /* Computed Parameter: D
                                         * '<S8>/voltage converter'
                                         */
  real_T motorelectrical_A;             /* Computed Parameter: A
                                         * '<S8>/motor electrical'
                                         */
  real_T motorelectrical_C;             /* Computed Parameter: C
                                         * '<S8>/motor electrical'
                                         */
  real_T motorelectrical_D;             /* Computed Parameter: D
                                         * '<S8>/motor electrical'
                                         */
  real_T motormechanical_A;             /* Computed Parameter: A
                                         * '<S8>/motor mechanical'
                                         */
  real_T motormechanical_C;             /* Computed Parameter: C
                                         * '<S8>/motor mechanical'
                                         */
  real_T motormechanical_D;             /* Computed Parameter: D
                                         * '<S8>/motor mechanical'
                                         */
  real_T phi_A;                         /* Computed Parameter: A
                                         * '<S8>/phi'
                                         */
  real_T phi_C;                         /* Computed Parameter: C
                                         * '<S8>/phi'
                                         */
  real_T phi_D;                         /* Computed Parameter: D
                                         * '<S8>/phi'
                                         */
  real_T Constant_Value;                /* Expression: 0
                                         * '<S1>/Constant'
                                         */
  real_T Internal_A[6];                 /* Computed Parameter: A
                                         * '<S10>/Internal'
                                         */
  real_T Internal_B[12];                /* Computed Parameter: B
                                         * '<S10>/Internal'
                                         */
  real_T Internal_C[2];                 /* Computed Parameter: C
                                         * '<S10>/Internal'
                                         */
  real_T Internal_D[6];                 /* Computed Parameter: D
                                         * '<S10>/Internal'
                                         */
  real_T rad2grad_Gain;                 /* Expression: 180/pi
                                         * '<S9>/rad2grad'
                                         */
  real_T speedsensor_A;                 /* Computed Parameter: A
                                         * '<S8>/speed sensor'
                                         */
  real_T speedsensor_C;                 /* Computed Parameter: C
                                         * '<S8>/speed sensor'
                                         */
  real_T speedsensor_D;                 /* Computed Parameter: D
                                         * '<S8>/speed sensor'
                                         */
  real_T phi1_A;                        /* Computed Parameter: A
                                         * '<S8>/phi1'
                                         */
  real_T phi1_C;                        /* Computed Parameter: C
                                         * '<S8>/phi1'
                                         */
  real_T phi1_D;                        /* Computed Parameter: D
                                         * '<S8>/phi1'
                                         */
};

/* Real-time Model Data Structure */
struct _RT_MODEL_carModel_Tag {
  const char *errorStatus;
};

/* Block parameters (auto storage) */
extern Parameters_carModel carModel_P;

/* Block states (auto storage) */
extern D_Work_carModel carModel_DWork;

/* External inputs (root inport signals with auto storage) */
extern ExternalInputs_carModel carModel_U;

/* External outputs (root outports fed by signals with auto storage) */
extern ExternalOutputs_carModel carModel_Y;

/* Model entry point functions */

extern void carModel_initialize(boolean_T firstTime);
extern void carModel_step(void);
extern void carModel_terminate(void);

/* Real-time Model object */
extern RT_MODEL_carModel *carModel_M;

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
 * '<Root>' : carModel
 * '<S1>'   : carModel/carModel
 * '<S2>'   : carModel/xx_LockDownSignalSpec_1
 * '<S3>'   : carModel/xx_LockDownSignalSpec_2
 * '<S4>'   : carModel/xx_LockDownSignalSpec_3
 * '<S5>'   : carModel/xx_LockDownSignalSpec_4
 * '<S6>'   : carModel/xx_LockDownSignalSpec_5
 * '<S7>'   : carModel/xx_LockDownSignalSpec_6
 * '<S8>'   : carModel/carModel/DC Servo
 * '<S9>'   : carModel/carModel/Lin Einspurmodell
 * '<S10>'  : carModel/carModel/Lin Einspurmodell/LTI System
 * '<S11>'  : carModel/carModel/Lin Einspurmodell/Subsystem
 * '<S12>'  : carModel/carModel/Lin Einspurmodell/LTI System/Tdi
 * '<S13>'  : carModel/carModel/Lin Einspurmodell/LTI System/Tdo
 */

#endif                                  /* _RTW_HEADER_carModel_h_ */

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
