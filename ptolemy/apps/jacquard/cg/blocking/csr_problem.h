#ifndef CSR_PROBLEM_H
#define CSR_PROBLEM_H


typedef struct csr_matrix_t {
    int m, n, nz;
    int *row_start;
    int *col_idx;

	///////////////////////////////
    int *currentCol;
	///////////////////////////////

	double *val;
} csr_matrix_t;


typedef struct csr_jacobi_t {
    int n;
    int block_size;
    double *factored_blocks;
} csr_jacobi_t;


csr_matrix_t *csr_mm_load(char *filename);
csr_matrix_t *csr_hb_load(char *filename);
void csr_matvec(double *Ax, void *Adata, double *x, int n);

#endif                          /* CSR_PROBLEM_H */
