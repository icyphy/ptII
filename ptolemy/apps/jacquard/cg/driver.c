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

double* jacobi_init(void* data);
void jacobi_psolve(double *Minvx, void *Mdata, double *x, int n);



#if !defined(L1_BLOCK_W)
#define L1_BLOCK_W 300	// **has to be multiple of REG_BLOCK_L and REG_BLOCK_W
#endif

#if !defined(L1_BLOCK_L)
#define L1_BLOCK_L 300	// **has to be multiple of REG_BLOCK_L and REG_BLOCK_W
#endif

#if !defined(REG_BLOCK_W)
#define REG_BLOCK_W 50
#endif

#if !defined(REG_BLOCK_L)
#define REG_BLOCK_L 50
#endif

void
initialize_timer (struct Timer * t)
{
  t->clock_holder.tv_sec = 0;
  t->clock_holder.tv_usec = 0;
  t->duration.tv_sec = 0;
  t->duration.tv_usec = 0;
}

void start_timer(struct Timer * t)
{
  gettimeofday (&t->clock_holder, NULL);
}

void stop_timer(struct Timer * t)
{
  struct timeval end_tv;
  gettimeofday (&end_tv, NULL);
  t->duration.tv_sec += (end_tv.tv_sec - t->clock_holder.tv_sec);
  t->duration.tv_usec += (end_tv.tv_usec - t->clock_holder.tv_usec);
}

double timer_duration(const struct Timer t)
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

#define min(x, y) ((x > y)? y : x)
/* Multiply by a matrix in compressed sparse-row format
 */

void register_matvec(double *Ax, void *Adata, double *x, int n, const int blockI, const int blockJ) {


	int i, j;

	csr_matrix_t *Acsr = (csr_matrix_t *) Adata;
    double *Aval = Acsr->val;
    int *Arow = Acsr->row_start;
    int *Acol = Acsr->col_idx;

	// FIXME: don't forget to initialize. (i.e. Ax[i] = 0).

	const int n_blocks_w = min(blockI + REG_BLOCK_W, n);
    const int n_blocks_l = min(blockJ + REG_BLOCK_L, n);

	// blockJ <= col < n_blocks_l
	//printf("blockI[%d], blockJ[%d], n_blocks_w[%d], n_blocks_l[%d]\n", blockI, blockJ, n_blocks_w, n_blocks_l);

	for (j = blockJ; j < n_blocks_l; ++j) {

		// initialize for every iteration.
		if (blockI == 0) {
			// initialize result vector.
			Ax[j] = 0;

			// initialize tmp index for column.
			Acsr->currentCol[j] = Arow[j];
		}

		// Do this to avoid read-write dependency.
		double Axj = Ax[j];

		for (i = Acsr->currentCol[j]; i < Arow[j + 1]; ++i) {

			if (Acol[i] >= blockI) {
				if (Acol[i] < n_blocks_w) {
					//printf("blockI[%d], blockJ[%d], row[%d], Acol[%d]\n", blockI, blockJ, j, Acol[k]);
					/*
					if (j == 0) {
						printf("blockI[%d], blockJ[%d], row[%d], Acol[%d], k[%d]\n", blockI, blockJ, j, Acol[k], k);
						//printf("row[%d], Acol[%d]\n", i, Acol[k]);
					}
					*/
					Axj += (Aval[i]) * x[Acol[i]];					
				} else {
					// Remember we left off from this column, so we can start from this next time.
					Acsr->currentCol[j] = i;

					// next row.
					break;
				}
			}
        }

		Ax[j] = Axj;
    }
}

//void register_do_block(double *Ax, void *Adata, double *x, int n, const int blockI, const int blockJ)
void register_do_block(double *Ax, void *Adata, double *x, int n)
{
    int i, j;
    const int n_blocks_w = blockI + L1_BLOCK_W;
    const int n_blocks_l = blockJ + L1_BLOCK_L;


	for (i = blockI; i < n_blocks_w; i+=REG_BLOCK_W) {
		for (j = blockJ; j < n_blocks_l; j+=REG_BLOCK_L) {
			register_matvec(Ax, Adata, x, n, i, j);			
        }
    }
}

void L1_do_block(double *Ax, void *Adata, double *x, int n)
{
    int i, j;

	for (i = 0; i < n; i+=L1_BLOCK_W) {
		for (j = 0; j < n; j+=L1_BLOCK_L) {
			register_do_block(Ax, Adata, x, n, i, j);			
        }
    }
}


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

//#define MAX_N   100
//#define MAX_NNZ 1000
/*
void csr_matvec(double *Ax, void *Adata, double *x, int n)
{
    int i, j;

    csr_matrix_t *Acsr = (csr_matrix_t *) Adata;
    int *Arow = Acsr->row_start;
    int *Acol = Acsr->col_idx;
    double *Aval = Acsr->val;
    int mystart = Acsr->myStart;

    static shared [] double xall[MAX_NNZ];

    for (i = 0; i < n; ++i) 
		xall[mystart + i] = x[i];

    upc_barrier;

    for (i = 0; i < n; ++i) {
        Ax[i] = 0.0;
        for (j = Arow[i]; j < Arow[i + 1]; ++j) {
            Ax[i] += (*Aval++) * xall[*Acol++];
        }
    }
}
*/

/* Main routine -- test out the code on a sample problem
 */
void driver(int m, int maxiter,
            void (*matvec) (double *, void *, double *, int), void *Adata,
            void (*psolve) (double *, void *, double *, int), void *Mdata,
            int n, int myStart)
{

    // m < n, m is the columns allocated for this processor
    double *b = (double *) malloc(m * sizeof(double));
    double *x = (double *) malloc(m * sizeof(double));
    double *rhist = NULL;

    //static shared [n] double xall[n * sizeof(double)];
    //static shared double* xall;
    static shared [] double xall[MAX_NNZ];
    //xall = (shared double* shared) upc_alloc(n * sizeof(double));

    double rtol = 1e-3;

    int i, retval;

    FILE *rhist_fp = NULL;
    FILE *x_fp = NULL;

    upc_barrier;

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

    retval = precond_cg(matvec, psolve, Adata, Mdata,
                        b, x, rtol, m, rhist, maxiter);

    for (i = 0; i < m; ++i) xall[myStart + i] = x[i];

    upc_barrier;

    if (MYTHREAD == 0) {

        for (i = 0; i < n; ++i)
            fprintf(x_fp, "%g\n", xall[i]);

        stop_timer(&total_timer);
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

void blocking_init(csr_matrix_t *A) {
	//printf("init\n");
    // FIXME: something is wrong with the malloc amount..
    A->currentCol = (int *) malloc((2 * A->nz - A->m) * sizeof(int));
	//A->currentCol = (int*) malloc(A->m * sizeof(int));
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
		blocking_init(A);
	    double* Aj = jacobi_init(A);
        printf("Using problem %s\n", argv[1]);

	if ( MYTHREAD == 0)
	  printf("With Jacobi: ");
        driver(A->m, A->n, register_do_block, A, jacobi_psolve, Aj, A->n, A->myStart);

/*         printf("With SSOR:         "); */
/*         driver(A->m, A->m, csr_matvec, A, csr_ssor_psolve, A, A->n, A->myStart); */

		if(MYTHREAD == 0) printf("Vanilla CG:        \n");

		driver(A->m, A->n, register_do_block, A, dummy_psolve, NULL, A->n, A->myStart);
    }

    return 0;
}
void jacobi_psolve(double *Minvx, void *Mdata, double *x, int n)
{
  //csr_matrix_t* Mj = (csr_matrix_t*)Mdata;
  double* Mj = (double*)Mdata;
  int i;

  //printf("thread %d entering psolve\n", MYTHREAD);
  for (i =0; i<n; i++)
    Minvx[i] = x[i] / Mj[i];

}


double* jacobi_init(void* data)
{
  csr_matrix_t* Mdata = (csr_matrix_t *) data;
  int m = Mdata->m;
  double* jMdata = (double*)malloc(sizeof(double) * m);
  int* Mrows = Mdata -> row_start;
  int* Mcols = Mdata -> col_idx;
  //double * jMval;
  int i, k;
  assert(jMdata != NULL);

  printf ("thread %d m = %d\n", MYTHREAD, m);

  //jMdata -> m = m;
  //jMdata -> n = Mdata -> n;
  //jMdata -> nz = m*m-m;
  //jMdata -> rowstart = (int*) malloc(sizeof(int) * m);
  //jMdata -> col_idx = (int*)malloc(sizeof(int) * m);

  //jMdata -> val = (double*)malloc (sizeof(double) * m);
  //jMdata = jMdata -> val;
  memset( jMdata, 0, m);
  
  assert ( jMdata != NULL );
  

  for ( i = 0; i < m; i++)
    for ( k = Mrows[i] ; k < Mrows[i+1]; k++) {
    /*   if (MYTHREAD == 2) */
/* 	printf ("row %d, col %d\n", i + MYTHREAD *m,Mcols[k]); */
	  if ( i + Mdata->myStart == Mcols[k] )
	    jMdata[i] = (Mdata -> val) [k];
    }

  /* if ( MYTHREAD == 2) { */
/*   for (i = 0; i < m; i++) */
/*     printf("%g, ", jMdata[i]); */
/*   printf("\n%d rows\n", m); */
/*   } */
  return jMdata;
}
