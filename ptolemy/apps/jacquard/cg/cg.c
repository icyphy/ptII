#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>


/* Compute alpha*x + y and store in dest (daxpy-like operation)
 *
 * Arguments:
 *  dest  - destination vector.  Can be the same as x or y.
 *  alpha - scalar multiplier
 *  x, y  - vector inputs
 *  n     - vector size
 */
void axpy(double *dest, double alpha, double *x, double *y, int n)
{
    int i;
    for (i = 0; i < n; ++i)
        dest[i] = alpha * x[i] + y[i];
}


/* Compute the dot product of two vectors x'*y
 *
 * Arguments:
 *  x, y - vector inputs
 *  n    - vector size
 */
double ddot(double *x, double *y, int n)
{
    int i;
    //double sum = 0;
    double localSum = 0;
    shared double *globalSum;
    globalSum = (shared double*) upc_alloc(sizeof(double));

    for (i = 0; i < n; ++i)
        localSum += x[i] * y[i];

    upc_barrier;

    *globalSum += localSum;

    return *globalSum;
    //return localSum;
}


/* Solve Ax = b using a preconditioned conjugate-gradient iteration.
 * 
 * Arguments:
 *  matvec(Ax,    Adata, x, n) - Multiply A*x into Ax
 *  psolve(Minvx, Mdata, x, n) - Apply preconditioner (solve M\x into Minvx)
 *  Adata - Opaque pointer to data used by matvec
 *  Mdata - Opaque pointer to data used by psolve
 *  b     - System right-hand side
 *  x     - Result
 *  rtol  - Tolerance on the relative residual (||r||/||b||)
 *  n     - System size
 *  rhist   - Residual norm history.  Should be at least maxiter doubles
 *            if it is not NULL.
 *  maxiter - Maximum number of iterations before returning
 *
 * Returns:
 *  Iteration count on success, -1 on failure
 */
int precond_cg(void (*matvec) (double *Ax, void *Adata, double *x, int n),
               void (*psolve) (double *Minvx, void *Adata, double *x,
                               int n), void *Adata, void *Mdata, double *b,
               double *x, double rtol, int n, double *rhist, int maxiter)
{
    const int nbytes = n * sizeof(double);

    double bnorm2;              /* ||b||^2 */
    double rnorm2;              /* Residual norm squared */
    double rz, rzold;           /* r'*z from two successive iterations */
    double alpha, beta;

    double *s;                  /* Search direction */
    double *r;                  /* Residual         */
    double *z;                  /* Temporary vector */

    int i;                      /* Current iteration */

    s = (double *) malloc(nbytes);
    r = (double *) malloc(nbytes);
    z = (double *) malloc(nbytes);

printf("T[%d]: this\n", MYTHREAD);
    bnorm2 = ddot(b, b, n);
printf("T[%d]: is not the problem\n", MYTHREAD);
    memset(x, 0, nbytes);
    memcpy(r, b, nbytes);
    psolve(z, Mdata, r, n);
    memcpy(s, z, nbytes);

    rz = ddot(r, z, n);
    rnorm2 = ddot(r, r, n);

printf("T[%d]: still not the problem\n", MYTHREAD);
    for (i = 0; i < maxiter && rnorm2 > bnorm2 * rtol * rtol; ++i) {

        if (rhist != NULL)
            rhist[i] = sqrt(rnorm2 / bnorm2);

        matvec(z, Adata, s, n);
        alpha = rz / ddot(s, z, n);
        axpy(x, alpha, s, x, n);
        axpy(r, -alpha, z, r, n);

        psolve(z, Mdata, r, n);
        rzold = rz;
        rz = ddot(r, z, n);
        rnorm2 = ddot(r, r, n);

        beta = -rz / rzold;
        axpy(s, -beta, s, z, n);

    }

printf("T[%d]: still not the problem2\n", MYTHREAD);
    free(z);
    free(r);
    free(s);

    if (i >= maxiter)
        return -1;

    if (rhist != NULL)
        rhist[i] = sqrt(rnorm2 / bnorm2);

    return i;
}


/* Dummy preconditioner... just the identity
 */
void dummy_psolve(double *Minvx, void *Mdata, double *x, int n)
{
    memcpy(Minvx, x, n * sizeof(double));
}
