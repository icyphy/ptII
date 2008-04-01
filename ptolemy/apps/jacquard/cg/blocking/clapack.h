#ifndef CLAPACK_H

void cholesky_factor(int n, double *A, int ldA);

void cholesky_solve(int n, int nrhs,
                    double *Afactor, int ldA, double *B, int ldB);

void band_cholesky_factor(int n, int kd, double *A, int ldA);

void band_cholesky_solve(int n, int kd, int nrhs,
                         double *Afactor, int ldA, double *B, int ldB);

#endif
