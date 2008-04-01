#ifndef CG_H
#define CG_H

void axpy(double *dest, double alpha, double *x, double *y, int n);

double ddot(double *x, double *y, int n);

int precond_cg(void (*matvec) (double *Ax, void *Adata, double *x, int n),
               void (*psolve) (double *Minvx, void *Adata, double *x,
                               int n), void *Adata, void *Mdata, double *b,
               double *x, double rtol, int n, double *rhist, int maxiter);

void dummy_psolve(double *Minvx, void *Mdata, double *x, int n);

#endif                          /* CG_H */
