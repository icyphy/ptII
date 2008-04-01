#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include <sys/time.h>
#include <time.h>

#include <bupc_collectivev.h>
#include "csr_problem.h"
#include "cg.h"

void initialize_timer (struct Timer * t);
void start_timer(struct Timer * t);
void stop_timer(struct Timer * t);
double timer_duration(const struct Timer t);

/* Compute alpha*x + y and store in dest (daxpy-like operation)
 *
 * Arguments:
 *  dest  - destination vector.  Can be the same as x or y.
 *  alpha - scalar multiplier
 *  x, y  - vector inputs
 *  n     - vector size
 */
/*
void axpy(double *dest, double alpha, double *x, double *y, int n)
{
    int i;
    for (i = 0; i < n; ++i)
        dest[i] = alpha * x[i] + y[i];
}
*/

#define axpy(dest, alpha, x, y, n) {	\
    int i;								\
    for (i = 0; i < n; ++i)				\
        dest[i] = alpha * x[i] + y[i];};

/* Compute the dot product of two vectors x'*y
 *
 * Arguments:
 *  x, y - vector inputs
 *  n    - vector size
 */
double ddot(double *x, double *y, int n)
{
    int i;
    double localSum = 0.0;
	//static shared double sum;

    for (i = 0; i < n; ++i)
        localSum += x[i] * y[i];

	return  bupc_allv_reduce_all(double, localSum, UPC_ADD);
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
               void (*psolve) (double *Minvx, void *Adata, double *x, int n), 
               void *Adata, void *Mdata, double *b,
               double *x, double rtol, int n, double *rhist, int maxiter)
{
    // n is actually m
    const int nbytes = n * sizeof(double);

    double bnorm2;              /* ||b||^2 */
    double rnorm2;              /* Residual norm squared */
    double rz, rzold;           /* r'*z from two successive iterations */
    double alpha, beta;

    double *s;                  /* Search direction */
	double *localSAll;
    double *r;                  /* Residual         */
    double *z;                  /* Temporary vector */

	//shared double* sall;

    int i;                      /* Current iteration */

    struct Timer total_timer;

	csr_matrix_t *Acsr = (csr_matrix_t *) Adata;
    int *Arow = Acsr->row_start;
    int *Acol = Acsr->col_idx;
    double *Aval = Acsr->val;
    int mystart = Acsr->myStart;
	int nall = Acsr->n;


    localSAll = (double *) malloc(nall * sizeof(double));
	s = &localSAll[mystart];
    r = (double *) malloc(nbytes);
    z = (double *) malloc(nbytes);

   	//assumming all threads have the same number of rows
	//and last threads have less
	//int allocSize = nall/THREADS + (nall%THREADS ? 1 : 0);
	//sall = (shared double *) upc_all_alloc(THREADS, allocSize * sizeof(double));
	static shared [] double sall[MAX_NNZ];

    bnorm2 = ddot(b, b, n);

    memset(x, 0, nbytes);
    memcpy(r, b, nbytes);

    // z is the preconditioned x?
    psolve(z, Mdata, r, n);
    //replaced with:
    //memcpy(z, r, nbytes);
    // s is the dummy variable used to solve?
    memcpy(s, z, nbytes);

	//upc_barrier;

	if(MYTHREAD == 0)
	{
		initialize_timer(&total_timer);
		start_timer(&total_timer);
	}

    // b dot x => z is the guess for x, r is copied b
    rz = ddot(r, z, n);

    rnorm2 = ddot(r, r, n);


    for (i = 0; i < maxiter && rnorm2 > bnorm2 * rtol * rtol; ++i) {

        if (rhist != NULL)
            rhist[i] = sqrt(rnorm2 / bnorm2);

        // matrix multiplied by vector:
        // z = A * s
        //matvec(z, Adata, s, n);

		//collect data to the shared memory space
		upc_memput(&sall[mystart], s, nbytes);
		
		upc_barrier;

		//copy data back
		int copy_size = Acsr->last_col - Acsr->first_col + 1;
		upc_memget(&localSAll[Acsr->first_col], &sall[Acsr->first_col], copy_size * sizeof(double));

		matvec(z, Adata, localSAll, n);


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

	upc_barrier;

	if(MYTHREAD == 0)
	{
		stop_timer(&total_timer);
		printf("time for the loop: %g\t", timer_duration(total_timer));
	}

    free(z);
    free(r);
    free(localSAll);

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

