#ifndef CG_H
#define CG_H

/* A simple wrapper around the clock() timer */
struct Timer {
  struct timeval clock_holder;
  struct timeval duration;
};

void initialize_timer (struct Timer* t);
void start_timer (struct Timer* t);
void stop_timer (struct Timer* t);
double timer_duration(const struct Timer t);

void axpy(double *dest, double alpha, double *x, double *y, int n);

double ddot(double *x, double *y, int n);

int precond_cg(void (*matvec) (double *Ax, void *Adata, double *x, int n),
               void (*psolve) (double *Minvx, void *Adata, double *x,
                               int n), void *Adata, void *Mdata, double *b,
               double *x, double rtol, int n, double *rhist, int maxiter);

void dummy_psolve(double *Minvx, void *Mdata, double *x, int n);

#endif                          /* CG_H */
