#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <sys/time.h>
#include <time.h>

#include <upc.h>
#include "cg.h"
#include "poisson_problem.h"
#include "csr_problem.h"

void
initialize_timer (struct Timer * t)
{
  t->clock_holder.tv_sec = 0;
  t->clock_holder.tv_usec = 0;
  t->duration.tv_sec = 0;
  t->duration.tv_usec = 0;
}

void
start_timer(struct Timer * t)
{
  gettimeofday (&t->clock_holder, NULL);
}

void
stop_timer(struct Timer * t)
{
  struct timeval end_tv;
  gettimeofday (&end_tv, NULL);
  t->duration.tv_sec += (end_tv.tv_sec - t->clock_holder.tv_sec);
  t->duration.tv_usec += (end_tv.tv_usec - t->clock_holder.tv_usec);
}

double
timer_duration(const struct Timer t)
{
  return t.duration.tv_sec + 1.0e-6 * (double)t.duration.tv_usec;
}

/* Test problem: 1-d Poisson operator
 */
void poisson_matvec(double *Ax, void *Adata, double *x, int n)
{
    int i;

    Ax[0] = 2 * x[0] - x[1];
    for (i = 1; i < n - 1; ++i)
        Ax[i] = -x[i - 1] + 2 * x[i] - x[i + 1];
    Ax[n - 1] = -x[n - 2] + 2 * x[n - 1];
}

/* Multiply by a matrix in compressed sparse-row format
 */
/*
void csr_matvec(double *Ax, void *Adata, double *x, int n)
{
    int i, j;

    csr_matrix_t *Acsr = (csr_matrix_t *) Adata;
    double *Aval = Acsr->val;
    int *Arow = Acsr->row_start;
    int *Acol = Acsr->col_idx;

    for (i = 0; i < n; ++i) {
        Ax[i] = 0;
        for (j = Arow[i]; j < Arow[i + 1]; ++j) {
            Ax[i] += (*Aval++) * x[*Acol++];
        }
    }
}
*/

//#define MAX_N   100
//#define MAX_NNZ 1000

void csr_matvec(double *Ax, void *Adata, double *x, int n)
{
    int i, j;

    csr_matrix_t *Acsr = (csr_matrix_t *) Adata;
    int *Arow = Acsr->row_start;
    int *Acol = Acsr->col_idx;
    double *Aval = Acsr->val;
    int my_start = Acsr->localStart[MYTHREAD];

    //static shared double xglobal[MAX_NNZ];
    static shared [] double xglobal[MAX_NNZ];
    //static shared double* xglobal;
    //xglobal = (shared double*) upc_all_alloc(MAX_NNZ, MAX_NNZ * sizeof(double));

    //upc_barrier;
    /* Copy local vector section into shared memory */
    for (i = 0; i < n; ++i) {
        xglobal[my_start + i] = x[i];
    }
    upc_barrier;

    for (i = 0; i < n; ++i) {
        Ax[i] = 0;
        for (j = Arow[i]; j < Arow[i + 1]; ++j) {
            int col = *Acol++;
            double Aelement = *Aval++;
            Ax[i] += Aelement * xglobal[col];
        }
    }
    //upc_free(xglobal);
    upc_barrier;
}


/* Main routine -- test out the code on a sample problem
 */
void driver(int m, int maxiter,
            void (*matvec) (double *, void *, double *, int), void *Adata,
            void (*psolve) (double *, void *, double *, int), void *Mdata,
            int n, int localStart)
{

    // m < n, m is the columns allocated for this processor
    double *b = (double *) malloc(m * sizeof(double));
    double *x = (double *) malloc(n * sizeof(double));
    double *rhist = NULL;

    static shared double *xall;
    xall = (shared double*) upc_alloc(n * sizeof(double));

    double rtol = 1e-3;

    int i, retval;

    FILE *rhist_fp = NULL;
    FILE *x_fp = NULL;

    upc_barrier;

    // register blocking...
    for (i = 0; i < m; ++i)
        b[i] = 1;

    struct Timer total_timer;

    if (MYTHREAD == 0) {
        initialize_timer(&total_timer);
        start_timer(&total_timer);

        rhist = (double *) malloc(maxiter * sizeof(double));
        rhist_fp = fopen("rhist.out", "w");
        x_fp = fopen("x.out", "w");

    }

    upc_barrier;

printf("T[%d]:here3\n", MYTHREAD);

printf("m = %d\n", m);

    retval = precond_cg(matvec, psolve, Adata, Mdata,
                        b, x, rtol, m, rhist, maxiter);

printf("T[%d]:here4\n", MYTHREAD);

    for (i = 0; i < n; ++i)
        xall[localStart + i] = x[i];

    upc_barrier;

    if (MYTHREAD == 0) {
  //stop_timer(&total_timer);

        for (i = 0; i < n; ++i)
            fprintf(x_fp, "%g\n", xall[i]);

        printf("total time taken: %g \n", timer_duration(total_timer));

        if (retval < 0) {
            printf("Iteration failed to converge!\n");
            for (i = 0; i < maxiter; ++i)
                fprintf(rhist_fp, "%g\n", rhist[i]);

        } else {
            printf("Converged after %d iterations\n", retval);
            for (i = 0; i <= retval; ++i)
                fprintf(rhist_fp, "%g\n", rhist[i]);
        }


        fclose(x_fp);
        fclose(rhist_fp);
        free(rhist);
    }
    free(x);
    free(b);

    upc_barrier;
}


int main(int argc, char **argv)
{
    if (argc <= 1) {

//        poisson_jacobi_t *M = poisson_jacobi_init(20);

        printf("Using default 1-d Poisson on a 500 point mesh\n");
//        driver(500, 500, poisson_matvec, NULL, poisson_jacobi_psolve, M);
        driver(500, 500, poisson_matvec, NULL, dummy_psolve, NULL, (int)NULL, (int)NULL);

    } else {
        int block_size = 60;

        csr_matrix_t *A = csr_hb_load(argv[1]);
//        csr_jacobi_t *Mj = csr_jacobi_init(A, block_size);

        printf("Using problem %s\n", argv[1]);
/*
        printf("With block Jacobi: ");
        driver(A->m, A->m, csr_matvec, A, csr_jacobi_psolve, Mj, A->n, A->localStart);

        printf("With SSOR:         ");
        driver(A->m, A->m, csr_matvec, A, csr_ssor_psolve, A, A->n, A->localStart);
*/

        printf("Vanilla CG:        ");
printf("T[%d]: here\n", MYTHREAD);
        driver(A->m, A->m, csr_matvec, A, dummy_psolve, NULL, A->n, A->localStart);
    }

printf("T[%d]: finished\n", MYTHREAD);
    return 0;
}
