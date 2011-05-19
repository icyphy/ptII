/*
 * File: motorController.h
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

#ifndef _RTW_HEADER_motorController_h_
#define _RTW_HEADER_motorController_h_

#include <math.h>
#include <float.h>
#include <string.h>
#include "../rtwtypes.h"
#include "../rtw_continuous.h"
#include "../rtw_solver.h"
#include "../rtlibsrc.h"

#include "motorController_types.h"

/* Macros for accessing real-time model data structure  */

#ifndef rtmGetErrorStatus
# define rtmGetErrorStatus(rtm) ((rtm)->errorStatus)
#endif

#ifndef rtmSetErrorStatus
# define rtmSetErrorStatus(rtm, val) ((rtm)->errorStatus = ((val)))
#endif

/* Block states (auto storage) for system: '<Root>' */
typedef struct D_Work_motorController_tag {
  real_T SpeedReg_DSTATE;               /* '<S1>/Speed Reg' */
  real_T CurrentReg_DSTATE;             /* '<S1>/Current Reg' */
} D_Work_motorController;

/* External inputs (root inport signals with auto storage) */
typedef struct _ExternalInputs_motorController_tag {
  real_T delta_r_sp;                    /* '<Root>/delta_r_sp' */
  real_T delta_r_act;                   /* '<Root>/delta_r_act' */
  real_T angular_rate;                  /* '<Root>/angular_rate' */
  real_T current;                       /* '<Root>/current' */
} ExternalInputs_motorController;

/* External outputs (root outports fed by signals with auto storage) */
typedef struct _ExternalOutputs_motorController_tag {
  real_T voltage;                       /* '<Root>/voltage' */
} ExternalOutputs_motorController;

/* Parameters (auto storage) */
struct _Parameters_motorController {
  real_T Kphi_Gain;                     /* Expression: 0.4
                                         * '<S1>/Kphi'
                                         */
  real_T SpeedReg_A;                    /* Computed Parameter: A
                                         * '<S1>/Speed Reg'
                                         */
  real_T SpeedReg_C;                    /* Computed Parameter: C
                                         * '<S1>/Speed Reg'
                                         */
  real_T SpeedReg_D;                    /* Computed Parameter: D
                                         * '<S1>/Speed Reg'
                                         */
  real_T CurrentReg_A;                  /* Computed Parameter: A
                                         * '<S1>/Current Reg'
                                         */
  real_T CurrentReg_C;                  /* Computed Parameter: C
                                         * '<S1>/Current Reg'
                                         */
  real_T CurrentReg_D;                  /* Computed Parameter: D
                                         * '<S1>/Current Reg'
                                         */
};

/* Real-time Model Data Structure */
struct _RT_MODEL_motorController_Tag {
  const char *errorStatus;
};

/* Block parameters (auto storage) */
extern Parameters_motorController motorController_P;

/* Block states (auto storage) */
extern D_Work_motorController motorController_DWork;

/* External inputs (root inport signals with auto storage) */
extern ExternalInputs_motorController motorController_U;

/* External outputs (root outports fed by signals with auto storage) */
extern ExternalOutputs_motorController motorController_Y;

/* Model entry point functions */

extern void motorController_initialize(boolean_T firstTime);
extern void motorController_step(void);
extern void motorController_terminate(void);

/* Real-time Model object */
extern RT_MODEL_motorController *motorController_M;

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
 * '<Root>' : motorController
 * '<S1>'   : motorController/motorController
 * '<S2>'   : motorController/xx_LockDownSignalSpec_1
 * '<S3>'   : motorController/xx_LockDownSignalSpec_2
 * '<S4>'   : motorController/xx_LockDownSignalSpec_3
 * '<S5>'   : motorController/xx_LockDownSignalSpec_4
 * '<S6>'   : motorController/xx_LockDownSignalSpec_5
 */

#endif                                  /* _RTW_HEADER_motorController_h_ */

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
