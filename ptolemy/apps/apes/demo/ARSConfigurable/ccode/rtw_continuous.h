/*
 * Copyright 1990-2004 The MathWorks, Inc.
 *
 * File: rtw_continuous.h     $Revision: 1.1.6.2.2.1 $
 *
 * Abstract:
 *   Type definitions for continuous-time support.
 *
 */

#ifndef __RTW_CONTINUOUS_H__
#define __RTW_CONTINUOUS_H__

/* For models registering MassMatrix */
typedef enum {
    SS_MATRIX_NONE,
    SS_MATRIX_CONSTANT,
    SS_MATRIX_TIMEDEP,
    SS_MATRIX_STATEDEP
} ssMatrixType;
    
typedef enum {
    SOLVER_MODE_AUTO,          /* only occurs in
                                  mdlInitializeSizes/mdlInitializeSampleTimes */
    SOLVER_MODE_SINGLETASKING,
    SOLVER_MODE_MULTITASKING
} SolverMode;

typedef enum {
    MINOR_TIME_STEP,
    MAJOR_TIME_STEP
} SimTimeStep;

/* =============================================================================
 * Model methods object
 * =============================================================================
 */
typedef void (*rtMdlInitializeSizesFcn)(void *rtModel);
typedef void (*rtMdlInitializeSampleTimesFcn)(void *rtModel);
typedef void (*rtMdlStartFcn)(void *rtModel);
typedef void (*rtMdlOutputsFcn)(void *rtModel, int_T tid);
typedef void (*rtMdlUpdateFcn)(void *rtModel, int_T tid);
typedef void (*rtMdlDerivativesFcn)(void *rtModel);
typedef void (*rtMdlProjectionFcn)(void *rtModel);
typedef void (*rtMdlTerminateFcn)(void *rtModel);

typedef struct _RTWRTModelMethodsInfo_tag {
    void                          *rtModelPtr;
    rtMdlInitializeSizesFcn       rtmInitSizesFcn;
    rtMdlInitializeSampleTimesFcn rtmInitSampTimesFcn;
    rtMdlStartFcn                 rtmStartFcn;
    rtMdlOutputsFcn               rtmOutputsFcn;
    rtMdlUpdateFcn                rtmUpdateFcn;
    rtMdlDerivativesFcn           rtmDervisFcn;
    rtMdlProjectionFcn            rtmProjectionFcn;
    rtMdlTerminateFcn             rtmTerminateFcn;
} RTWRTModelMethodsInfo;

#define rtmiSetRTModelPtr(M,rtmp) ((M).rtModelPtr = (rtmp))
#define rtmiGetRTModelPtr(M)      (M).rtModelPtr

#define rtmiSetInitSizesFcn(M,fp) \
  ((M).rtmInitSizesFcn = ((rtMdlInitializeSizesFcn)(fp)))
#define rtmiSetInitSampTimesFcn(M,fp) \
  ((M).rtmInitSampTimesFcn = ((rtMdlInitializeSampleTimesFcn)(fp)))
#define rtmiSetStartFcn(M,fp) \
  ((M).rtmStartFcn = ((rtMdlStartFcn)(fp)))
#define rtmiSetOutputsFcn(M,fp) \
  ((M).rtmOutputsFcn = ((rtMdlOutputsFcn)(fp)))
#define rtmiSetUpdateFcn(M,fp) \
  ((M).rtmUpdateFcn = ((rtMdlUpdateFcn)(fp)))
#define rtmiSetDervisFcn(M,fp) \
  ((M).rtmDervisFcn = ((rtMdlDerivativesFcn)(fp)))
#define rtmiSetProjectionFcn(M,fp) \
  ((M).rtmProjectionFcn = ((rtMdlProjectionFcn)(fp)))
#define rtmiSetTerminateFcn(M,fp) \
  ((M).rtmTerminateFcn = ((rtMdlTerminateFcn)(fp)))

#define rtmiInitializeSizes(M) \
         (*(M).rtmInitSizesFcn)((M).rtModelPtr)
#define rtmiInitializeSampleTimes(M) \
         (*(M).rtmInitSampTimesFcn)((M).rtModelPtr)
#define rtmiStart(M) \
         (*(M).rtmStartFcn)((M).rtModelPtr)
#define rtmiOutputs(M, tid) \
         (*(M).rtmOutputsFcn)((M).rtModelPtr,tid)
#define rtmiUpdate(M, tid) \
        (*(M).rtmUpdateFcn)((M).rtModelPtr,tid)
#define rtmiDerivatives(M) \
         (*(M).rtmDervisFcn)((M).rtModelPtr)
#define rtmiProjection(M) \
         (*(M).rtmProjectionFcn)((M).rtModelPtr)
#define rtmiTerminate(M) \
         (*(M).rtmTerminateFcn)((M).rtModelPtr)

#endif /* __RTW_CONTINUOUS_H__ */
