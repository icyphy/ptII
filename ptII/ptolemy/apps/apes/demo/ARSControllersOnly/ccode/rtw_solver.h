/*
 * Copyright 1990-2004 The MathWorks, Inc.
 *
 * File: rtw_solver.h     $Revision: 1.1.6.2.2.1 $
 *
 * Abstract:
 *   Type definitions for continuous-time solver support.
 *
 */

#ifndef __RTW_SOLVER_H__
#define __RTW_SOLVER_H__

/* =============================================================================
 * Solver object
 * =============================================================================
 */
#ifndef NO_FLOATS /* ERT integer-only */

typedef struct _ssSolverInfo_tag {
    void        *rtModelPtr;

    SimTimeStep *simTimeStepPtr;
    void        *solverData;
    const char  *solverName;
    boolean_T   isVariableStepSolver;
    boolean_T   solverNeedsReset;
    SolverMode  solverMode;

    time_T      solverStopTime;
    time_T      *stepSizePtr;
    time_T      minStepSize;
    time_T      maxStepSize;
    time_T      fixedStepSize;

    int_T       maxNumMinSteps;
    int_T       solverMaxOrder;
    int_T       solverExtrapolationOrder;
    int_T       solverNumberNewtonIterations;

    int_T       solverRefineFactor;
    real_T      solverRelTol;
    real_T      solverAbsTol;

    real_T      **dXPtr;
    time_T      **tPtr;

    int_T       *numContStatesPtr;
    real_T      **contStatesPtr;

    int_T       numZcSignals;
    real_T*     zcSignal;
    uint8_T*    zcSignalDirs;

    boolean_T   stateProjection;
    boolean_T   advancedZcDetection;
    boolean_T   consistencyChecking;

    ssMatrixType  massMatrixType;

    const char_T **errStatusPtr;

    RTWRTModelMethodsInfo *modelMethodsPtr;
} ssSolverInfo;

/* Support old name RTWSolverInfo */
typedef ssSolverInfo RTWSolverInfo;

#define rtsiSetRTModelPtr(S,rtmp) ((S)->rtModelPtr = (rtmp))
#define rtsiGetRTModelPtr(S)      (S)->rtModelPtr

#define rtsiSetSimTimeStepPtr(S,stp) ((S)->simTimeStepPtr = (stp))
#define rtsiGetSimTimeStepPtr(S) ((S)->simTimeStepPtr)
#define rtsiGetSimTimeStep(S)        *((S)->simTimeStepPtr)
#define rtsiSetSimTimeStep(S,st)     (*((S)->simTimeStepPtr) = (st))

#define rtsiSetSolverData(S,sd) ((S)->solverData = (sd))
#define rtsiGetSolverData(S)    (S)->solverData

#define rtsiSetSolverName(S,sn) ((S)->solverName = (sn))
#define rtsiGetSolverName(S)    (S)->solverName

#define rtsiSetVariableStepSolver(S,vs) ((S)->isVariableStepSolver = (vs))
#define rtsiIsVariableStepSolver(S)     (S)->isVariableStepSolver

#define rtsiSetSolverNeedsReset(S,sn) ((S)->solverNeedsReset = (sn))
#define rtsiGetSolverNeedsReset(S)    (S)->solverNeedsReset

#define rtsiSetSolverMode(S,sm) ((S)->solverMode = (sm))
#define rtsiGetSolverMode(S)    (S)->solverMode

#define rtsiSetSolverStopTime(S,st) ((S)->solverStopTime = (st))
#define rtsiGetSolverStopTime(S)    (S)->solverStopTime

#define rtsiSetStepSizePtr(S,ssp) ((S)->stepSizePtr = (ssp))
#define rtsiSetStepSize(S,ss)     (*((S)->stepSizePtr) = (ss))
#define rtsiGetStepSize(S)        *((S)->stepSizePtr)

#define rtsiSetMinStepSize(S,ss) (((S)->minStepSize = (ss)))
#define rtsiGetMinStepSize(S)    (S)->minStepSize

#define rtsiSetMaxStepSize(S,ss) ((S)->maxStepSize = (ss))
#define rtsiGetMaxStepSize(S)    (S)->maxStepSize

#define rtsiSetFixedStepSize(S,ss) ((S)->fixedStepSize = (ss))
#define rtsiGetFixedStepSize(S)    (S)->fixedStepSize

#define rtsiSetMaxNumMinSteps(S,mns) ((S)->maxNumMinSteps = (mns))
#define rtsiGetMaxNumMinSteps(S)     (S)->maxNumMinSteps

#define rtsiSetSolverMaxOrder(S,smo) ((S)->solverMaxOrder = (smo))
#define rtsiGetSolverMaxOrder(S)     (S)->solverMaxOrder

#define rtsiSetSolverExtrapolationOrder(S,seo) ((S)->solverExtrapolationOrder = (seo))
#define rtsiGetSolverExtrapolationOrder(S)      (S)->solverExtrapolationOrder

#define rtsiSetSolverNumberNewtonIterations(S,nni) ((S)->solverNumberNewtonIterations = (nni))
#define rtsiGetSolverNumberNewtonIterations(S)      (S)->solverNumberNewtonIterations

#define rtsiSetSolverRefineFactor(S,smo) ((S)->solverRefineFactor = (smo))
#define rtsiGetSolverRefineFactor(S)     (S)->solverRefineFactor

#define rtsiSetSolverRelTol(S,smo) ((S)->solverRelTol = (smo))
#define rtsiGetSolverRelTol(S)     (S)->solverRelTol

#define rtsiSetSolverAbsTol(S,smo) ((S)->solverAbsTol = (smo))
#define rtsiGetSolverAbsTol(S)     (S)->solverAbsTol

#define rtsiSetdXPtr(S,dxp) ((S)->dXPtr = (dxp))
#define rtsiSetdX(S,dx)     (*((S)->dXPtr) = (dx))
#define rtsiGetdX(S)        *((S)->dXPtr)

#define rtsiSetTPtr(S,tp) ((S)->tPtr = (tp))
#define rtsiSetT(S,t)     ((*((S)->tPtr))[0] = (t))
#define rtsiGetT(S)       (*((S)->tPtr))[0]

#define rtsiSetContStatesPtr(S,cp) ((S)->contStatesPtr = (cp))
#define rtsiGetContStates(S)       *((S)->contStatesPtr)

#define rtsiSetNumContStatesPtr(S,cp) ((S)->numContStatesPtr = (cp))
#define rtsiGetNumContStates(S)       *((S)->numContStatesPtr)

#define rtsiSetErrorStatusPtr(S,esp) ((S)->errStatusPtr = (esp))
#define rtsiSetErrorStatus(S,es) (*((S)->errStatusPtr) = (es))
#define rtsiGetErrorStatus(S)    *((S)->errStatusPtr)

#define rtsiSetModelMethodsPtr(S,mmp) ((S)->modelMethodsPtr = (mmp))
#define rtsiGetModelMethodsPtr(S)     (S)->modelMethodsPtr

#endif /* !NO_FLOATS */

#endif /* __RTW_SOLVER_H__ */
