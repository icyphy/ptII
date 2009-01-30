#ifndef rtlibsrc_h
#define rtlibsrc_h

/* Copyright 1994-2003 The MathWorks, Inc.
 *
 * File    : rtlibsrc.h
 * Abstract:
 *      Function prototypes for .c sources in rtw/c/libsrc.
 *
 */

/* $Revision: 1.54.4.5 $ */

/*==========*
 * Includes *
 *==========*/

#include "rtwtypes.h"
#include <limits.h>

#if defined(INTEGER_CODE)
# if INTEGER_CODE == 0
#  undef INTEGER_CODE
# endif
#endif

/*=========*
 * Asserts *
 *=========*/
#ifndef utAssert 
# if defined(DOASSERTS)
#  if !defined(PRINT_ASSERTS)
#    include <assert.h>
#    define utAssert(exp)  assert(exp)
#  else
     static void _assert(char *statement, char *file, int line)
     {
        printf("%s in %s on line %d\n", statement, file, line);
     }
#   define utAssert(_EX) ((_EX) ? (void)0 : _assert(#_EX, __FILE__, __LINE__))
#  endif
# else
#  define utAssert(exp) /* do nothing */
# endif
#endif

/*==========================*
 * Visible/extern functions *
 *==========================*/

#if !defined(INTEGER_CODE)

typedef enum {
  NORMAL_INTERP,
  AVERAGE_VALUE,
  MIDDLE_VALUE
} ZeroTechnique;

extern real_T rt_atan2(real_T a, real_T b);
extern real32_T rt_atan232(real32_T a, real32_T b);
extern real_T rt_hypot(real_T a, real_T b);
extern real32_T rt_hypot32(real32_T a, real32_T b);
extern int_T  rt_GetLookupIndex(const real_T *x, int_T xlen, real_T u);
extern real_T rt_Lookup(const real_T *x, int_T xlen, 
                        real_T u, const real_T *y);
extern real_T rt_Lookup2D_General (const real_T *rowVals, 
                                   const int_T numRowVals,
                                   const real_T *colVals, 
                                   const int_T numColVals,
                                   const real_T *outputValues,
                                   const real_T uX, const real_T uY,
                                   const int_T colZeroIdx,
                                   const ZeroTechnique colZeroTechnique,
                                   const real_T *outputAtRowZero);
extern real_T rt_Lookup2D_Normal (const real_T *xVals, const int_T numX,
                                  const real_T *yVals, const int_T numY,
                                  const real_T *zVals,
                                  const real_T x, const real_T y);
extern int_T    rt_GetLookupIndex32(const real32_T *x, int_T xlen, real32_T u);
extern real32_T rt_Lookup32(const real32_T *x, int_T xlen, 
                            real32_T u, const real32_T *y);
extern real32_T rt_Lookup2D32_General (const real32_T *rowVals, 
                                   const int_T numRowVals,
                                   const real32_T *colVals, 
                                   const int_T numColVals,
                                   const real32_T *outputValues,
                                   const real32_T uX, const real32_T uY,
                                   const int_T colZeroIdx,
                                   const ZeroTechnique colZeroTechnique,
                                   const real32_T *outputAtRowZero);
extern real32_T rt_Lookup2D32_Normal (const real32_T *xVals, const int_T numX,
                                  const real32_T *yVals, const int_T numY,
                                  const real32_T *zVals,
                                  const real32_T x, const real32_T y);
extern real_T rt_Urand(uint_T *seed);
extern real_T rt_NormalRand(uint_T *seed);

/* Matrix Multiplication Utility Functions */
extern void rt_MatMultRR_Dbl(real_T       *y, 
                             const real_T *A, 
                             const real_T *B, 
                             const int    dims[3]);

#ifdef CREAL_T
extern void rt_MatMultRC_Dbl(creal_T       *y,
                             const real_T  *A,
                             const creal_T *B,
                             const int     dims[3]);

extern void rt_MatMultCR_Dbl(creal_T       *y,
                             const creal_T *A,
                             const real_T  *B,
                             const int     dims[3]);

extern void rt_MatMultCC_Dbl(creal_T       *y,
                             const creal_T *A,
                             const creal_T *B,
                             const int     dims[3]);
#endif

extern void rt_MatMultRR_Sgl(real32_T       *y,
                             const real32_T *A,
                             const real32_T *B,
                             const int      dims[3]);

#ifdef CREAL_T
extern void rt_MatMultRC_Sgl(creal32_T       *y,
                             const real32_T  *A,
                             const creal32_T *B,
                             const int       dims[3]);

extern void rt_MatMultCR_Sgl(creal32_T       *y,
                             const creal32_T *A,
                             const real32_T  *B,
                             const int       dims[3]);

extern void rt_MatMultCC_Sgl(creal32_T       *y,
                             const creal32_T *A,
                             const creal32_T *B,
                             const int       dims[3]);
#endif

extern void rt_MatMultAndIncRR_Dbl(real_T       *y,
                                   const real_T *A,
                                   const real_T *B,
                                   const int    dims[3]);

#ifdef CREAL_T
extern void rt_MatMultAndIncRC_Dbl(creal_T       *y,
                                   const real_T  *A,
                                   const creal_T *B,
                                   const int     dims[3]);

extern void rt_MatMultAndIncCR_Dbl(creal_T       *y,
                                   const creal_T *A,
                                   const real_T  *B,
                                   const int     dims[3]);

extern void rt_MatMultAndIncCC_Dbl(creal_T       *y,
                                   const creal_T *A,
                                   const creal_T *B,
                                   const int     dims[3]);
#endif
extern void rt_MatMultAndIncRR_Sgl(real32_T       *y,
                                   const real32_T *A,
                                   const real32_T *B,
                                   const int      dims[3]);

#ifdef CREAL_T
extern void rt_MatMultAndIncRC_Sgl(creal32_T       *y,
                                   const real32_T  *A,
                                   const creal32_T *B,
                                   const int       dims[3]);

extern void rt_MatMultAndIncCR_Sgl(creal32_T       *y,
                                   const creal32_T *A,
                                   const real32_T  *B,
                                   const int       dims[3]);

extern void rt_MatMultAndIncCC_Sgl(creal32_T       *y,
                                   const creal32_T *A,
                                   const creal32_T *B,
                                   const int       dims[3]);
#endif 

/* Matrix Inversion Utility Functions */
extern void rt_lu_real(real_T      *A,
                       const int_T n,
                       int32_T     *piv);

#ifdef CREAL_T
extern void rt_lu_cplx(creal_T *A,
                       const int_T n,
                       int32_T *piv);
#endif

extern void rt_lu_real_sgl(real32_T      *A,
                           const int_T n,
                           int32_T     *piv);

#ifdef CREAL_T
extern void rt_lu_cplx_sgl(creal32_T *A,
                           const int_T n,
                           int32_T *piv);
#endif

extern void rt_BackwardSubstitutionRR_Dbl(real_T    *pU,
                                          real_T    *pb,
                                          real_T    *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);

#ifdef CREAL_T
extern void rt_BackwardSubstitutionRC_Dbl(real_T    *pU,
                                          creal_T   *pb,
                                          creal_T   *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);

extern void rt_BackwardSubstitutionCC_Dbl(creal_T   *pU,
                                          creal_T   *pb,
                                          creal_T   *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);
#endif

extern void rt_ForwardSubstitutionRR_Dbl(real_T    *pL,
                                         real_T    *pb,
                                         real_T    *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

#ifdef CREAL_T
extern void rt_ForwardSubstitutionRC_Dbl(real_T    *pL,
                                         creal_T   *pb,
                                         creal_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

extern void rt_ForwardSubstitutionCR_Dbl(creal_T   *pL,
                                         real_T    *pb,
                                         creal_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

extern void rt_ForwardSubstitutionCC_Dbl(creal_T   *pL,
                                         creal_T   *pb,
                                         creal_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);
#endif

extern void rt_BackwardSubstitutionRR_Sgl(real32_T    *pU,
                                          real32_T    *pb,
                                          real32_T    *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);

#ifdef CREAL_T
extern void rt_BackwardSubstitutionRC_Sgl(real32_T    *pU,
                                          creal32_T   *pb,
                                          creal32_T   *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);

extern void rt_BackwardSubstitutionCC_Sgl(creal32_T   *pU,
                                          creal32_T   *pb,
                                          creal32_T   *x,
                                          int_T     N,
                                          int_T     P,
                                          boolean_T unit_upper);
#endif

extern void rt_ForwardSubstitutionRR_Sgl(real32_T    *pL,
                                         real32_T    *pb,
                                         real32_T    *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

#ifdef CREAL_T
extern void rt_ForwardSubstitutionRC_Sgl(real32_T    *pL,
                                         creal32_T   *pb,
                                         creal32_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

extern void rt_ForwardSubstitutionCR_Sgl(creal32_T   *pL,
                                         real32_T    *pb,
                                         creal32_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);

extern void rt_ForwardSubstitutionCC_Sgl(creal32_T   *pL,
                                         creal32_T   *pb,
                                         creal32_T   *x,
                                         int_T     N,
                                         int_T     P,
                                         int32_T   *piv,
                                         boolean_T unit_lower);
#endif


extern void rt_MatDivRR_Dbl(real_T    *Out,
                            real_T    *In1,
                            real_T    *In2,
                            real_T    *lu,
                            int32_T   *piv,
                            real_T    *x,
                            const int dims[3]);

#ifdef CREAL_T
extern void rt_MatDivRC_Dbl(creal_T   *Out,
                            real_T    *In1,
                            creal_T   *In2,
                            real_T    *lu,
                            int32_T   *piv,
                            creal_T   *x,
                            const int dims[3]);

extern void rt_MatDivCR_Dbl(creal_T   *Out,
                            creal_T   *In1,
                            real_T    *In2,
                            creal_T   *lu,
                            int32_T   *piv,
                            creal_T   *x,
                            const int dims[3]);

extern void rt_MatDivCC_Dbl(creal_T   *Out,
                            creal_T   *In1,
                            creal_T   *In2,
                            creal_T   *lu,
                            int32_T   *piv,
                            creal_T   *x,
                            const int dims[3]);
#endif

extern void rt_MatDivRR_Sgl(real32_T    *Out,
                            real32_T    *In1,
                            real32_T    *In2,
                            real32_T    *lu,
                            int32_T   *piv,
                            real32_T    *x,
                            const int dims[3]);

#ifdef CREAL_T
extern void rt_MatDivRC_Sgl(creal32_T   *Out,
                            real32_T    *In1,
                            creal32_T   *In2,
                            real32_T    *lu,
                            int32_T   *piv,
                            creal32_T   *x,
                            const int dims[3]);

extern void rt_MatDivCR_Sgl(creal32_T   *Out,
                            creal32_T   *In1,
                            real32_T    *In2,
                            creal32_T   *lu,
                            int32_T   *piv,
                            creal32_T   *x,
                            const int dims[3]);

extern void rt_MatDivCC_Sgl(creal32_T   *Out,
                            creal32_T   *In1,
                            creal32_T   *In2,
                            creal32_T   *lu,
                            int32_T   *piv,
                            creal32_T   *x,
                            const int dims[3]);
#endif

extern ZCEventType rt_ZCFcn(ZCDirection direction,
                            ZCSigState *prevSigState,
                            real_T      zcSig);

extern ZCEventType rt_I32ZCFcn(ZCDirection direction,
                               ZCSigState *prevSigState,
                               int32_T      zcSig);

extern real_T rt_TDelayInterpolate(
    real_T     tDelay,         /* t = simTime - delay */
    real_T     *tBuf,
    real_T     *uBuf,
    int_T      bufSz,
    int_T      *lastIdx,
    int_T      oldestIdx,
    int_T      newIdx,
    boolean_T  discrete,
    boolean_T  minorStepAndTAtLastMajorOutput);

extern boolean_T rt_TDelayUpdateTailOrGrowBuf(
    int_T  *bufSzPtr,
    int_T  *oldestIdxPtr,
    int_T  *newestIdxPtr,
    int_T  *cachedIdxPtr,
    real_T tMinusDelay,
    real_T **uBufPtr,
    real_T **tBufPtr,
    int    *maxNewBufSzPtr);


/*=========*
 * Defines *
 *=========*/

# define MAXSEED  2147483646   /* 2^31-2 */
# define SEED0    1144108930   /* Seed #6, starting from seed = 1 */
# define RT_BIT16      32768   /* 2^15   */

#define RT_PI          3.14159265358979323846
#define RT_LN_10       2.30258509299404568402
#define RT_LOG10E      0.43429448190325182765
#define RT_E           2.7182818284590452354

#define INTERP(x,x1,x2,y1,y2) ( y1+((y2 - y1)/(x2 - x1))*(x-x1) )

/* Matrix multiplication defines */
#ifndef CMULT_RE
  #define CMULT_RE(X,Y) ( (X).re * (Y).re - (X).im * (Y).im)
  #define CMULT_IM(X,Y) ( (X).re * (Y).im + (X).im * (Y).re)
#endif

#define CMULT_XCONJ_RE(X,Y) ( (X).re * (Y).re + (X).im * (Y).im)
#define CMULT_XCONJ_IM(X,Y) ( (X).re * (Y).im - (X).im * (Y).re)

#define CMULT_YCONJ_RE(X,Y) ( (X).re * (Y).re + (X).im * (Y).im)
#define CMULT_YCONJ_IM(X,Y) (-(X).re * (Y).im + (X).im * (Y).re)

#define CMULT_XYCONJ_RE(X,Y) ( (X).re * (Y).re - (X).im * (Y).im)
#define CMULT_XYCONJ_IM(X,Y) (-(X).re * (Y).im - (X).im * (Y).re)

/* Complex conjugate: */
#define CCONJ(X,Y) { (Y).re = (X).re; (Y).im = -((X).im); }

/* Complex magnitude squared ( X * conj(X), or |X|^2 ) */
#define CMAGSQ(X) ((X).re * (X).re + (X).im * (X).im)

/* Quick-and-dirty (approximate) complex absolute value: */
#define CQABS(X) (fabs((X).re) + fabs((X).im))

/* Quick-and-dirty (approximate) complex absolute value for singles: */
#define CQABSSGL(X) (real32_T)(fabs( (real_T) (X).re) + fabs( (real_T) (X).im)) 

#ifdef CREAL_T
/* Complex reciprocal: C = 1 / B  (A=1) */
#define CRECIP(B,C) { const real_T _s = 1.0 / CQABS(B); real_T _d; creal_T _bs; _bs.re = (B).re * _s; _bs.im = (B).im * _s; _d = 1.0 / CMAGSQ(_bs); (C).re = ( _s * _bs.re) * _d; (C).im = (-_s * _bs.im) * _d; }

/* Complex reciprocal: C = 1 / B  (A=1) */
#define CRECIPSGL(B,C) { const real32_T _s = 1.0F / CQABSSGL(B); real32_T _d; creal32_T _bs; _bs.re = (B).re * _s; _bs.im = (B).im * _s; _d = 1.0F / CMAGSQ(_bs); (C).re = ( _s * _bs.re) * _d; (C).im = (-_s * _bs.im) * _d; }

/* Complex division: C = A / B for singles */
#define CDIV(A,B,C) { if ((B).im == 0.0) { (C).re = (A).re / (B).re; (C).im = (A).im / (B).re; } else { const real_T _s = 1.0 / CQABS(B); real_T _d; creal_T _as, _bs; _as.re = (A).re * _s; _as.im = (A).im * _s; _bs.re = (B).re * _s; _bs.im = (B).im * _s; _d = 1.0 / CMAGSQ(_bs); (C).re = CMULT_YCONJ_RE(_as, _bs) * _d; (C).im = CMULT_YCONJ_IM(_as, _bs) * _d; } }

/* Complex division: C = A / B for singles*/
#define CDIVSGL(A,B,C) { if ((B).im == 0.0F) { (C).re = (A).re / (B).re; (C).im = (A).im / (B).re; } else { const real32_T _s = 1.0F / CQABSSGL(B); real32_T _d; creal32_T _as, _bs; _as.re = (A).re * _s; _as.im = (A).im * _s; _bs.re = (B).re * _s; _bs.im = (B).im * _s; _d = 1.0F / CMAGSQ(_bs); (C).re = CMULT_YCONJ_RE(_as, _bs) * _d; (C).im = CMULT_YCONJ_IM(_as, _bs) * _d; } }
#endif


/* Hypotenuse: c = sqrt(a^2 + b^2) */
#define CHYPOT(A,B,C) { if (fabs(A) > fabs(B)) { real_T _tmp = (B)/(A); (C) = (fabs(A)*sqrt(1+_tmp*_tmp)); } else { if ((B) == 0.0) { (C) = 0.0; } else { real_T _tmp = (A)/(B); (C) = (fabs(B)*sqrt(1+_tmp*_tmp)); } } }

/* Complex modulus: Y = abs(X) */
#define CABS(X,Y) CHYPOT((X).re, (X).im, (Y))

#define rt_FSGN(u1)   ( ((u1)   >= 0.0F) ? ((u1) > 0.0F ? 1.0F : 0.0F) : -1.0F  )

# define rt_SET_RZC_SIGSTATE(sig) ( ((sig) < 0.0) ? NEG_ZCSIG : \
                                  ((sig) > 0.0 ? POS_ZCSIG : ZERO_ZCSIG) )

#endif /* !defined(INTEGER_CODE) */


/* INTEGER ONLY CODE BELOW THIS POINT */

#define rt_ABS(u1)    ( ((u1)   >= 0   ) ? (u1)                        : -(u1)  )
#define rt_MAX(u1,u2) ( ((u1)   >= (u2)) ? (u1)                        : (u2)   )
#define rt_MIN(u1,u2) ( ((u1)   <= (u2)) ? (u1)                        : (u2)   )
#define rt_MOD(u1,u2) ( (u1) % (u2) )
#define rt_REM(u1,u2) ( (u1) % (u2) )
#define rt_SATURATE(sig,ll,ul)                                                   \
                      ( ((sig ) >= (ul)) ? (ul)                        :        \
                        (((sig) <= (ll)) ? (ll)                        : (sig)) )
#define rt_SGN(u1)    ( ((u1)   >= 0   ) ? ((u1) > 0    ? 1    : 0   ) : -1     )
#define rt_UNSGN(u1)  ( ((u1)   >  0U  ) ? 1U                          : 0U     )

# define rt_SET_IZC_SIGSTATE(sig) ( ((sig) < 0) ? NEG_ZCSIG : \
                                  ((sig) > 0 ? POS_ZCSIG : ZERO_ZCSIG) )

#define rt_SET_UZC_SIGSTATE(sig) ((sig > 0)? POS_ZCSIG : ZERO_ZCSIG)

#define rt_SET_BZC_SIGSTATE(sig) ((sig)? POS_ZCSIG : ZERO_ZCSIG)


#ifndef TRUE
# define TRUE (1)
#endif
#ifndef FALSE
# define FALSE (0)
#endif

extern CondStates rt_EnableState(boolean_T enableTest, int_T prevEnableState);

/* Saturation Utility Functions */
extern int8_T   SaturateProduct_int8_T   (int8_T   a, int8_T b);
extern int16_T  SaturateProduct_int16_T  (int16_T  a, int16_T b);
extern int32_T  SaturateProduct_int32_T  (int32_T  a, int32_T b);
extern uint8_T  SaturateProduct_uint8_T  (uint8_T  a, uint8_T b);
extern uint16_T SaturateProduct_uint16_T (uint16_T a, uint16_T b);
extern uint32_T SaturateProduct_uint32_T (uint32_T a, uint32_T b);
extern int8_T   SaturateDivide_int8_T    (int8_T   a, int8_T b);
extern int16_T  SaturateDivide_int16_T   (int16_T  a, int16_T b);
extern int32_T  SaturateDivide_int32_T   (int32_T  a, int32_T b);
extern uint8_T  SaturateDivide_uint8_T   (uint8_T  a, uint8_T b);
extern uint16_T SaturateDivide_uint16_T  (uint16_T a, uint16_T b);
extern uint32_T SaturateDivide_uint32_T  (uint32_T a, uint32_T b);

#endif /* rtlibsrc_h */
