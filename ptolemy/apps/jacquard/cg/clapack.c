#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>


#ifdef _CRAY
typedef struct {
    char *s;
    unsigned long len;
} fstring;
#endif


void cholesky_factor(int n, double *A, int ldA)
{
    int info;
    char *uplo = "U";

#ifdef _CRAY
    extern int SPOTRF();
    fstring fuplo;
    fuplo.s = uplo;
    fuplo.len = 1;
    SPOTRF(fuplo, &n, A, &ldA, &info);
#else
    extern int dpotrf_();
    dpotrf_(uplo, &n, A, &ldA, &info);
#endif

    assert(info == 0);
}


void cholesky_solve(int n, int nrhs,
                    double *Afactor, int ldA, double *B, int ldB)
{
    int info;
    char *uplo = "U";

#ifdef _CRAY
    extern int SPOTRS();
    fstring fuplo;
    fuplo.s = uplo;
    fuplo.len = 1;
    SPOTRS(fuplo, &n, &nrhs, Afactor, &ldA, B, &ldB, &info);
#else
    extern int dpotrs_();
    dpotrs_(uplo, &n, &nrhs, Afactor, &ldA, B, &ldB, &info);
#endif

    assert(info == 0);
}


void band_cholesky_factor(int n, int kd, double *A, int ldA)
{
    int info;
    char *uplo = "U";

#ifdef _CRAY
    extern int SPBTRF();
    fstring fuplo;
    fuplo.s = uplo;
    fuplo.len = 1;
    SPBTRF(fuplo, &n, &kd, A, &ldA, &info);
#else
    extern int dpbtrf_();
    dpbtrf_(uplo, &n, &kd, A, &ldA, &info);
#endif

    assert(info == 0);
}


void band_cholesky_solve(int n, int kd, int nrhs,
                         double *Afactor, int ldA, double *B, int ldB)
{
    int info;
    char *uplo = "U";

#ifdef _CRAY
    extern SPBTRS();
    fstring fuplo;
    fuplo.s = uplo;
    fuplo.len = 1;
    SPBTRS(fuplo, &n, &kd, &nrhs, Afactor, &ldA, B, &ldB, &info);
#else
    extern int dpbtrs_();
    dpbtrs_(uplo, &n, &kd, &nrhs, Afactor, &ldA, B, &ldB, &info);
#endif

    assert(info == 0);
}
