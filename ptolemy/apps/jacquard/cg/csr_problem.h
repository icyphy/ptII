#ifndef CSR_PROBLEM_H
#define CSR_PROBLEM_H

#define MAX_THREADS 100

typedef struct csr_matrix_t {
    int m, n, nz;
    int *row_start;
    int *col_idx;
    double *val;

    int localStart;
} csr_matrix_t;


typedef struct csr_jacobi_t {
    int n;
    int block_size;
    double *factored_blocks;
} csr_jacobi_t;


csr_matrix_t *csr_mm_load(char *filename);
csr_matrix_t *csr_hb_load(char *filename);

void csr_matvec(double *Ax, void *Adata, double *x, int n);

//void csr_jacobi_psolve(double *Minvx, void *Mdata, double *x, int n);
//csr_jacobi_t *csr_jacobi_init(csr_matrix_t * A, int block_size);

#endif                          /* CSR_PROBLEM_H */
