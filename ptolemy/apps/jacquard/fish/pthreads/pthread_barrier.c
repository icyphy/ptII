#include <stdio.h>

#include <errno.h>
#include <pthread.h>
#if !defined(PTHREAD_BARRIER_SERIAL_THREAD)
#include "pthread_barrier.h"


int
pthread_barrier_destroy (pthread_barrier_t* bar)
{
  int err, err2;

  if (!bar) return EINVAL;

  err = pthread_mutex_trylock (&bar->mtx);

  if (err) return err;

  if (bar->n_threads_left != bar->n_threads_required) {
    err = EBUSY;
    goto err_done;
  }
  if (!bar->valid) {
    err = EINVAL;
    goto err_done;
  }

  bar->valid = 0; /* It's officially dead. */

  pthread_mutex_unlock (&bar->mtx);

  err = pthread_mutex_destroy (&bar->mtx);
  err2 = pthread_cond_destroy (&bar->cv);
  if (err) return err;
  return err2;

 err_done:
  pthread_mutex_unlock (&bar->mtx);
  return err;
}

int
pthread_barrier_init (pthread_barrier_t* bar,
		      const pthread_barrierattr_t* attr,
		      unsigned nthr)
{
  int err;

  if (!bar) return EINVAL;

  if (0 == nthr) return EINVAL;

  err = pthread_mutex_init (&bar->mtx, NULL);
  if (err) return err;
  err = pthread_cond_init (&bar->cv, NULL);
  if (err) {
    pthread_mutex_destroy (&bar->mtx);
    return err;
  }
  bar->n_threads_required = nthr;
  bar->n_threads_left = nthr;
  bar->cycle = 0;
  bar->valid = 1;

  return 0;
}

int
pthread_barrier_wait (pthread_barrier_t* bar_)
{
  volatile pthread_barrier_t* bar = bar_;
  int tmp;
  int err = 0;
  int cycle;

  if (!bar || !bar->valid) return EINVAL;

  err = pthread_mutex_lock (&bar_->mtx);
  if (err) return err;

  cycle = bar->cycle;

  tmp = --bar->n_threads_left;
  if (0 == tmp) {
    bar->n_threads_left = bar->n_threads_required;
    bar->cycle ^= 0x01;
    err = pthread_cond_broadcast (&bar_->cv);
    if (!err) err = PTHREAD_BARRIER_SERIAL_THREAD;
  }
  else {
    while (cycle == bar->cycle) {
      err = pthread_cond_wait (&bar_->cv, &bar_->mtx);
    }
  }

  pthread_mutex_unlock (&bar_->mtx);

  return err;
}

int
pthread_barrierattr_destroy (pthread_barrierattr_t* attr /*NOTUSED*/)
{
  if (!attr) return EINVAL;
  return 0;
}

int
pthread_barrierattr_getpshared (const pthread_barrierattr_t* attr /*NOTUSED*/,
				int* pshared)
{
  if (!attr) return EINVAL;
  *pshared = PTHREAD_PROCESS_PRIVATE;
  return 0;
}

int
pthread_barrierattr_init (pthread_barrierattr_t* attr)
{
  if (!attr) return EINVAL;
  return 1;
}

int
pthread_barrierattr_setpshared (pthread_barrierattr_t* attr, int pshared)
{
  if (!attr) return EINVAL;
  if (pshared != PTHREAD_PROCESS_PRIVATE)
    return ENOTSUP; /* XXX: Non-standard. */
  return 0;
}
#endif
