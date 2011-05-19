/*
 * File: dynamicsControll_private.h
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

#ifndef _RTW_HEADER_dynamicsControll_private_h_
#define _RTW_HEADER_dynamicsControll_private_h_

/* Private Macros used by the generated code to access rtModel */

/* Check for inclusion of an incorrect version of rtwtypes.h */
#ifndef RTWTYPES_ID_C08S16I32L32N32F1
#error This code was generated with a different "rtwtypes.h" than the file included
#endif
/* Check for inclusion of an incorrect version of rtwtypes.h */
#ifndef RTWTYPES_ID_C08S16I32L32N32F1
#error This code was generated with a different "rtwtypes.h" than the file included
#endif

void BINARYSEARCH_real_T( unsigned int *piLeft, unsigned int *piRght, real_T u,
 const real_T *pData, unsigned int iHi);
void LookUp_real_T_real_T_NEAR( real_T *pY, const real_T *pYData, real_T u,
 const real_T *pUData, unsigned int iHi);
void LookUp_real_T_real_T_SAT_NEAR( real_T *pY, const real_T *pYData, real_T u,
 const real_T *pUData, unsigned int iHi);

#endif                                  /* _RTW_HEADER_dynamicsControll_private_h_ */

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
