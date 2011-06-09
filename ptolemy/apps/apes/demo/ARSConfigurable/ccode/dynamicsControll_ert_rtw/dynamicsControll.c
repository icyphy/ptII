/*
 * File: dynamicsControll.c
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

#include <stdio.h>
#include "dynamicsControll.h"
#include "dynamicsControll_private.h"
#include "../motorController_ert_rtw/motorController.h" 
#include "../ARSmain.h"

extern double delta_f, speedProfile, yawrate, rearangle, anglerate, motorcurrent;

/* Block signals (auto storage) */
BlockIO_dynamicsControll dynamicsControll_B;

/* Block states (auto storage) */
D_Work_dynamicsControll dynamicsControll_DWork;

/* External inputs (root inport signals with auto storage) */
ExternalInputs_dynamicsControll dynamicsControll_U;

/* External output (root outports fed by signals with auto storage) */
ExternalOutputs_dynamicsControll dynamicsControll_Y;

/* Real-time model */
RT_MODEL_dynamicsControll dynamicsControll_M_;
RT_MODEL_dynamicsControll *dynamicsControll_M = &dynamicsControll_M_;

/*********************************************************************
 * Fixed-Point Binary Search Utility BINARYSEARCH_real_T
 */
void BINARYSEARCH_real_T( unsigned int *piLeft, unsigned int *piRght, real_T u,
 const real_T *pData, unsigned int iHi)
{
  /* Find the location of current input value in the data table. */
  *piLeft = 0;
  *piRght = iHi;

  if ( u <= pData[0] )
  {
    /* Less than or equal to the smallest point in the table. */
    *piRght = 0;
  }
  else if ( u >= pData[iHi] )
  {
    /* Greater than or equal to the largest point in the table. */
    *piLeft = iHi;
  }
  else
  {
    unsigned int i;

    /* Do a binary search. */
    while ( ( *piRght - *piLeft ) > 1 )
    {
      /* Get the average of the left and right indices using to Floor rounding. */
      i = (*piLeft + *piRght) >> 1;

      /* Move either the right index or the left index so that */
      /*  LeftDataPoint <= CurrentValue < RightDataPoint */
      if ( u < pData[i] )
      {
        *piRght = i;
      }
      else
      {
        *piLeft = i;
      }
    }
  }
}

/* end macro BINARYSEARCH_real_T
 *********************************************************************/

/*********************************************************************
 * Fixed-Point Lookup Utility LookUp_real_T_real_T_NEAR
 */
void LookUp_real_T_real_T_NEAR( real_T *pY, const real_T *pYData, real_T u,
 const real_T *pUData, unsigned int iHi)
{
  unsigned int iLeft;
  unsigned int iRght;
  BINARYSEARCH_real_T( &(iLeft), &(iRght), u, pUData, iHi);
  {
    real_T lambda;
    if ( pUData[iRght] > pUData[iLeft] )
    {
      real_T num;
      real_T den;

      den = pUData[iRght];
      den -= pUData[iLeft];
      num = u;
      num -= pUData[iLeft];
      {
        lambda = (num/den);
      }
    }
    else
    {
      lambda = 0;
    }
    {
      real_T yLeftCast;
      real_T yRghtCast;
      yLeftCast = pYData[iLeft];
      yRghtCast = pYData[iRght];
      yLeftCast += lambda * ( yRghtCast - yLeftCast );
      (*pY) = yLeftCast;
    }
  }
}

/* end function LookUp_real_T_real_T_NEAR
 *********************************************************************/

/*********************************************************************
 * Fixed-Point Lookup Utility LookUp_real_T_real_T_SAT_NEAR
 */
void LookUp_real_T_real_T_SAT_NEAR( real_T *pY, const real_T *pYData, real_T u,
 const real_T *pUData, unsigned int iHi)
{
  unsigned int iLeft;
  unsigned int iRght;
  BINARYSEARCH_real_T( &(iLeft), &(iRght), u, pUData, iHi);
  {
    real_T lambda;
    if ( pUData[iRght] > pUData[iLeft] )
    {
      real_T num;
      real_T den;

      den = pUData[iRght];
      den -= pUData[iLeft];
      num = u;
      num -= pUData[iLeft];
      {
        lambda = (num/den);
      }
    }
    else
    {
      lambda = 0;
    }
    {
      real_T yLeftCast;
      real_T yRghtCast;
      yLeftCast = pYData[iLeft];
      yRghtCast = pYData[iRght];
      yLeftCast += lambda * ( yRghtCast - yLeftCast );
      (*pY) = yLeftCast;
    }
  }
}

/* end function LookUp_real_T_real_T_SAT_NEAR
 *********************************************************************/

/* Model step function */
void dynamicsControll_step(void)
{

  dynamicsControll_updateInput();

  /* SubSystem: '<Root>/dynamicsController' */

  dynamicsController();

  /* Outport: '<Root>/delta_r_sp[rad]' */
  dynamicsControll_Y.delta_r_sprad = dynamicsControll_B.grad2rad;

  /* (no update code required) */
}

/* Model initialize function */
void dynamicsControll_initialize(boolean_T firstTime)
{

  if (firstTime) {
    /* registration code */

    /* initialize error status */
    rtmSetErrorStatus(dynamicsControll_M, (const char_T *)0);

    {
      /* block I/O */
      void *b = (void *) &dynamicsControll_B;

      {

        int_T i;
        b =&dynamicsControll_B.grad2rad;
        for (i = 0; i < 1; i++) {
          ((real_T*)b)[i] = 0.0;
        }
      }
    }

    /* data type work */
    dynamicsControll_DWork.DiscreteTransferFcn_DSTATE = 0.0;

    /* external inputs */
    dynamicsControll_U.delta_f = 0.0;
    dynamicsControll_U.vms = 0.0;
    dynamicsControll_U.yawd_acts = 0.0;
    dynamicsControll_U.delta_rrad = 0.0;

    /* external outputs */
    dynamicsControll_Y.delta_r_sprad = 0.0;
  }
}

/*update inputs*/
void dynamicsControll_updateInput(void)
{
	callbackI(0.01, 0.0, "delta_f");
    dynamicsControll_U.delta_f = delta_f;
	callbackI(0.01, 0.0, "speed");
    dynamicsControll_U.vms = speedProfile;
	callbackI(0.01, 0.0, "yawrate");
    dynamicsControll_U.yawd_acts = yawrate;
	callbackI(0.01, 0.0, "rearangle");
    dynamicsControll_U.delta_rrad = rearangle;
	
}

/* Model terminate function */
void dynamicsControll_terminate(void)
{
  /* (no terminate code required) */
}

/* File trailer for Real-Time Workshop generated code.
 *
 * You can customize this file trailer by specifying a different template.
 *
 * [EOF]
 */
