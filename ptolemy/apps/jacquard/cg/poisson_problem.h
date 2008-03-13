#ifndef POISSON_PROB_H
#define POISSON_PROB_H


typedef struct {
    double *data;
    double block_size;
} poisson_jacobi_t;


void poisson_matvec(double *Ax, void *Adata, double *x, int n);

#endif                          /* POISSON_PROB_H */
