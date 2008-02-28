#if !defined(PTHREAD_BARRIER_SUBST_)
#define PTHREAD_BARRIER_SUBST_

typedef struct pthread_barrier_t {
  pthread_mutex_t mtx;
  pthread_cond_t cv;
  int valid;
  int n_threads_required;
  int n_threads_left;
  int cycle; /* alternate wait cycles (0 or 1) */
} pthread_barrier_t;

typedef struct pthread_barrierattr_t {
  /* Not implemented. */
  /* First, AIX doesn't have barriers.  Then xlc requires
     a member.  augh. */
  char c;
} pthread_barrierattr_t;

#define PTHREAD_BARRIER_SERIAL_THREAD -1

int pthread_barrier_init (pthread_barrier_t*, const pthread_barrierattr_t*,
			 unsigned);
int pthread_barrier_destroy (pthread_barrier_t*);
int pthread_barrier_wait (pthread_barrier_t*);

int pthread_barrierattr_init (pthread_barrierattr_t*);
int pthread_barrierattr_destroy (pthread_barrierattr_t*);
int pthread_barrierattr_getpshared (const pthread_barrierattr_t*, int*);
int pthread_barrierattr_setpshared (pthread_barrierattr_t*, int);

#endif /* PTHREAD_BARRIER_SUBST_ */
