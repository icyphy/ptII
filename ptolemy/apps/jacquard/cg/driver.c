#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <math.h>
#include <sys/time.h>
#include <time.h>

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



/* Main routine -- test out the code on a sample problem
 */
void driver(int n, int maxiter,
            void (*matvec) (double *, void *, double *, int), void *Adata,
            void (*psolve) (double *, void *, double *, int), void *Mdata)
{
    double *b = (double *) malloc(n * sizeof(double));
    double *x = (double *) malloc(n * sizeof(double));
    double *rhist = (double *) malloc(maxiter * sizeof(double));

    double rtol = 1e-3;

    int i, retval;

    FILE *rhist_fp = fopen("rhist.out", "w");
    FILE *x_fp = fopen("x.out", "w");

    for (i = 0; i < n; ++i)
        b[i] = 1;

  struct Timer total_timer;
  initialize_timer(&total_timer);
  start_timer(&total_timer);

    retval = precond_cg(matvec, psolve, Adata, Mdata,
                        b, x, rtol, n, rhist, maxiter);

  stop_timer(&total_timer);

  printf("total time taken: %g \n", timer_duration(total_timer));

    for (i = 0; i < n; ++i)
        fprintf(x_fp, "%g\n", x[i]);

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
    free(x);
    free(b);
}


int main(int argc, char **argv)
{
    if (argc <= 1) {

//        poisson_jacobi_t *M = poisson_jacobi_init(20);

        printf("Using default 1-d Poisson on a 500 point mesh\n");
//        driver(500, 500, poisson_matvec, NULL, poisson_jacobi_psolve, M);
        driver(500, 500, poisson_matvec, NULL, dummy_psolve, NULL);

    } else {
        int block_size = 60;

        csr_matrix_t *A = csr_mm_load(argv[1]);
//        csr_jacobi_t *Mj = csr_jacobi_init(A, block_size);

        printf("Using problem %s\n", argv[1]);
/*
        printf("With block Jacobi: ");
        driver(A->m, A->m, csr_matvec, A, csr_jacobi_psolve, Mj);

        printf("With SSOR:         ");
        driver(A->m, A->m, csr_matvec, A, csr_ssor_psolve, A);
*/

        printf("Vanilla CG:        ");
        driver(A->m, A->m, csr_matvec, A, dummy_psolve, NULL);
    }

    return 0;
}
