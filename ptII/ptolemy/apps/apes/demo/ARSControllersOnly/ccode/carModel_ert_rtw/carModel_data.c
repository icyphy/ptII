/*
 * File: carModel_data.c
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

/* Block parameters (auto storage) */
Parameters_carModel carModel_P = {
  5.2083333333333336E-002 ,             /* i_v_Gain : '<S9>/1//i_v' */
  1.7453292519943295E-002 ,             /* grad2rad1_Gain : '<S9>/grad2rad1' */
  0.0 ,                                 /* UnitDelay_X0 : '<S8>/Unit Delay' */
  0.025 ,                               /* Mr_Gain : '<S8>/Mr' */
  0.0 ,                                 /* UnitDelay_X0_d : '<S1>/Unit Delay' */
  8.1818181818181823E-001 ,             /* voltageconverter_A : '<S8>/voltage converter' */
  1.6528925619834711E-001 ,             /* voltageconverter_C : '<S8>/voltage converter' */
  9.0909090909090912E-002 ,             /* voltageconverter_D : '<S8>/voltage converter' */
  9.8019801980198018E-001 ,             /* motorelectrical_A : '<S8>/motor electrical' */
  9.8029604940692086E-003 ,             /* motorelectrical_C : '<S8>/motor electrical' */
  4.9504950495049506E-003 ,             /* motorelectrical_D : '<S8>/motor electrical' */
  1.0 ,                                 /* motormechanical_A : '<S8>/motor mechanical' */
  1.0000000000000002E-001 ,             /* motormechanical_C : '<S8>/motor mechanical' */
  5.0000000000000010E-002 ,             /* motormechanical_D : '<S8>/motor mechanical' */
  1.0 ,                                 /* phi_A : '<S8>/phi' */
  0.001 ,                               /* phi_C : '<S8>/phi' */
  0.0005 ,                              /* phi_D : '<S8>/phi' */
  0.0 ,                                 /* Constant_Value : '<S1>/Constant' */
  /*  Internal_A : '<S10>/Internal' */
  { -9.6361731551697671E-001, 1.0, 1.9632779335950723E+000,
    -9.6361731551697671E-001, 1.0, 1.9632779335950723E+000 } ,
  /*  Internal_B : '<S10>/Internal' */
  { -6.4250283217427082E-002, -6.4235204613053154E-002,
    -8.4151013608486620E-011, 6.4767850001604080E-002, 6.6410408840615998E-002,
    -9.1668292228352709E-009, -2.0642708091764062E-001, 2.1799346017870730E-001,
    -2.3707416416046212E-006, 2.0999598139316525E-001, -2.2156036157834683E-001,
    2.4105790922635952E-006 } ,
  /*  Internal_C : '<S10>/Internal' */
  { 0.125, 0.5 } ,
  /*  Internal_D : '<S10>/Internal' */
  { 4.1068746756169772E-003, 4.1586802411908872E-003, -2.8911766271587466E-010,
    5.3021921377323165E-002, -5.5967092814157210E-002, 6.0879147908890247E-007 }
  ,
  5.7295779513082323E+001 ,             /* rad2grad_Gain : '<S9>/rad2grad' */
  8.1818181818181823E-001 ,             /* speedsensor_A : '<S8>/speed sensor' */
  1.6528925619834711E-002 ,             /* speedsensor_C : '<S8>/speed sensor' */
  9.0909090909090905E-003 ,             /* speedsensor_D : '<S8>/speed sensor' */
  8.1818181818181823E-001 ,             /* phi1_A : '<S8>/phi1' */
  3.3057851239669422E-002 ,             /* phi1_C : '<S8>/phi1' */
  1.8181818181818181E-002               /* phi1_D : '<S8>/phi1' */
};

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
