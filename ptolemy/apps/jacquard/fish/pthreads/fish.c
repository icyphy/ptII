#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>
#include <string.h>

#include <pthread.h>
#if !defined(HAVE_PTHREAD_BARRIER_WAIT) && !(PTHREAD_BARRIER_SERIAL_THREAD)
#include "pthread_barrier.h"
#endif

#include "config.h"
#include "fish.h"

FILE *output_fp; /* Pointer to output file */
int outputp;

int n_fish = 10; /* Other defaults in fish-lib.c. */
fish_t *fish; /* Global array of fish */

/* Structure to wrap up arguments passed into a thread  */
typedef struct thread_arg {
  int thread_id;
  pthread_t td;
	
  int n_local_fish;
  fish_t *local_fish;

  struct Timer total_timer;
  struct Timer barrier_timer;
} thread_arg_t;

pthread_barrier_t barrier;

#define MIN_FISH_PER_THREAD 2
void split_sequence (thread_arg_t* arg, const int n_thds, const int n_fish);
void init_fish();
void interact_fish(fish_t* local_fish_, const int n_local_fish,
		   const fish_t* fish_, const int n_fish);
void bounce_fish (fish_t* fish);
void move_fish (fish_t* fish, const int n_fish, const double dt);
double par_compute_norm (const fish_t* local_fish, const int n_local_fish,
			 struct Timer* barrier_timer);
double compute_norm (const fish_t* fish, const int n_fish);
void output_fish(FILE* output_fp, const double t, const double dt,
		 const fish_t* fish, const int n_fish);

int n_threads = 2;

struct arginfo pthread_args[] =
  {
    { 'p', INTARG, &n_threads, "Number of threads" },
    { 0, NULLARG, NULL, NULL },
  };

/* The main routine for each thread. */
static void *thread_routine(void *_arg)
{
  thread_arg_t *arg = (thread_arg_t *) _arg;

  struct Timer *total_timer = &arg->total_timer;
  struct Timer *barrier_timer = &arg->barrier_timer;

  const int n_local_fish = arg->n_local_fish;
  fish_t *local_fish = arg->local_fish;

  double curr_time;
  double output_time;
  double dt = 0.0;
  double max_norm;
  int steps;
  int barrier_return;

  initialize_timer (total_timer);
  initialize_timer (barrier_timer);

  /* cancellation semantics are scary. */
  pthread_setcancelstate (PTHREAD_CANCEL_DISABLE, NULL);

  /* --- Main time step loop --- */

  start_timer(total_timer);

  for (output_time = 0.0, curr_time = 0.0, steps = 0;
       curr_time <= end_time && steps < max_steps;
       curr_time += dt, ++steps) {

    /*
      We make sure that everyone is in sync at the start of a time
      step... why?
    */
    start_timer(barrier_timer);
    barrier_return = pthread_barrier_wait(&barrier);
    stop_timer(barrier_timer);

    if (outputp && curr_time >= output_time) {

      /*
	A normal barrier call returns 0 to all but one of the
	threads.  That one receives a magic number.  (We're ignoring
	error conditions here.)
      */
      if (barrier_return == PTHREAD_BARRIER_SERIAL_THREAD) {
	output_fish(output_fp, curr_time, dt, fish, n_fish);
      }
      output_time = curr_time + output_interval;
    }

    /*
      Now step the fish forward.  What synchronization exists in this
      code?  Is it sufficient?
    */

    interact_fish(local_fish, n_local_fish, fish, n_fish);

    start_timer (barrier_timer);
    pthread_barrier_wait (&barrier);
    stop_timer (barrier_timer);

    max_norm = par_compute_norm (local_fish, n_local_fish,
				 barrier_timer);

    dt = max_norm_change / max_norm;
    dt = f_max (dt, min_dt);
    dt = f_min (dt, max_dt);

    move_fish(local_fish, n_local_fish, dt);
  }

  start_timer(barrier_timer);
  barrier_return = pthread_barrier_wait(&barrier);
  stop_timer(barrier_timer);

  stop_timer(total_timer);

  if (outputp && barrier_return == PTHREAD_BARRIER_SERIAL_THREAD) {
    output_fish(output_fp, curr_time, dt, fish, n_fish);
    printf("Ended at %g (%g), %d (%d) steps\n",
	   curr_time, end_time, steps, max_steps);
  }

  return NULL;
}

int
main (int argc, char **argv)
{
  int i;
  pthread_attr_t attr;
  thread_arg_t *thread_arg;

  double total_time = 0.0;
  double barrier_time = 0.0;

  ext_args = pthread_args;
  get_options(argc, argv);
  srand48(clock());

  if (output_filename) {
    outputp = 1;
    output_fp = fopen(output_filename, "w");
    if (output_fp == NULL) {
      printf("Could not open %s for output\n", output_filename);
      exit(1);
    }
    fprintf(output_fp, "n_fish: %d\n", n_fish);
  }

  init_fish ();
  pthread_barrier_init (&barrier, NULL, n_threads);

  /*
    Roughly, the scope is supposed to be the level at which threads
    compete for processor resources.  System scope means they compete
    with other threads/processes in the system.  Process scope means
    they compete among themselves.  That's the theory, at any rate.
    What it means in practice varies widely.  In the case of
    LinuxThreads, you want system scope so that your threads can
    actually run on different processors.  I think.  If anyone knows
    better, let me know.
   */
  pthread_attr_init (&attr);
  pthread_attr_setscope (&attr, PTHREAD_SCOPE_SYSTEM);
  pthread_attr_setdetachstate (&attr, PTHREAD_CREATE_JOINABLE);

  /* --- Allocate and run threads --- */

  thread_arg = (thread_arg_t *) malloc(n_threads * sizeof(thread_arg_t));

  split_sequence (thread_arg, n_threads, n_fish);

  initialize_timer(&thread_arg[0].total_timer);
  initialize_timer(&thread_arg[0].barrier_timer);

  thread_arg[0].thread_id = 0;
  thread_arg[0].td = pthread_self ();
  for (i = 1; i < n_threads; ++i) {
    int err;

    thread_arg[i].thread_id = i;
    initialize_timer(&thread_arg[i].total_timer);
    initialize_timer(&thread_arg[i].barrier_timer);
    err = pthread_create (&thread_arg[i].td, &attr, thread_routine,
			  &thread_arg[i]);
    if (err) {
      const char* estr;
      fprintf (stderr, "Error creating thread %d: ", i);
      estr = strerror(err);
      if (estr) fprintf (stderr, "%s\n", estr);
      else fprintf (stderr, "%d\n", err);
      return -1;
    }
  }

  thread_routine (&thread_arg[0]);

  /* --- Wait for thread completion --- */

  total_time = timer_duration (thread_arg[0].total_timer);
  barrier_time = timer_duration (thread_arg[0].barrier_timer);
  for (i = 1; i < n_threads; ++i) {
    pthread_join(thread_arg[i].td, NULL);
    total_time += timer_duration (thread_arg[i].total_timer);
    barrier_time += timer_duration (thread_arg[i].barrier_timer);
  }

  /* --- Wrap it all up --- */

  if (outputp)
    fclose(output_fp);

  printf("Number of threads: %d\n"
	 "Total time taken: %g (%g)\n"
	 "Time in barriers: %g (%g)\n",
	 n_threads,
	 total_time, total_time / n_threads,
	 barrier_time, barrier_time / n_threads);

  return 0;
}








double
par_compute_norm (const fish_t* local_fish, const int n_local_fish,
		  struct Timer* barrier_timer)
{
  /*
    Both max_norm_lock and max_norm are shared variables, so we can't
    just declare them as ordinary automatic (stack) variables.  As
    static variables, max_norm and max_norm_lock live in the shared
    global address space; but as far as scoping rules go, they aren't
    visible outside this function.
  */
  static pthread_mutex_t max_norm_lock = PTHREAD_MUTEX_INITIALIZER;
  static double max_norm;
  double local_max_norm;

  max_norm = 0.0;

  /*
    Why does this routine need barriers?  How could you remove the
    barriers, or at least move them back to the main loop?
  */
  start_timer (barrier_timer);
  pthread_barrier_wait (&barrier);
  stop_timer (barrier_timer);

  /*
    These are essentially two different methods for performing
    a reduction operation.  Which is faster?  Why?
  */
#if 1
  pthread_mutex_lock (&max_norm_lock);
  max_norm = f_max (max_norm, compute_norm (local_fish, n_local_fish));
  pthread_mutex_unlock (&max_norm_lock);
#else
  local_max_norm = compute_norm (local_fish, n_local_fish);

  pthread_mutex_lock(&max_norm_lock);
  max_norm = f_max (max_norm, local_max_norm);
  pthread_mutex_unlock(&max_norm_lock);
#endif

  start_timer (barrier_timer);
  pthread_barrier_wait (&barrier);
  stop_timer (barrier_timer);

  return max_norm;
}


/* Partition out a section of the work to each processor. */
void
split_sequence (thread_arg_t* arg, const int n_thds, const int n_fish)
{
  int n_per = (n_fish + n_thds - 1) / n_thds;
  int idx, num, t;

  if (n_per < MIN_FISH_PER_THREAD) n_per = MIN_FISH_PER_THREAD;

  idx = 0;
  num = n_per;
  for (t = 0; t < n_thds; ++t) {
    if (idx + num > n_fish)
      num = n_fish - idx;

    arg[t].local_fish = &fish[idx];
    arg[t].n_local_fish = num;

    idx += num;
  }
}


/* Compute the global maximum of all velocities / accelerations.  */
double
compute_norm (const fish_t* fish, const int n_fish)
{
  int i;
  double max_norm = 0.0;

  for (i = 0; i < n_fish; ++i) {
    max_norm = f_max (max_norm, fabs(fish[i].vx));
    max_norm = f_max (max_norm, fabs(fish[i].vy));
    max_norm = f_max (max_norm, fabs(fish[i].ax));
    max_norm = f_max (max_norm, fabs(fish[i].ay));
  }

  return max_norm;
}


/* Compute the accelerations (force/mass) for each fish */
void
interact_fish(fish_t* local_fish_, const int n_local_fish,
	      const fish_t* fish_, const int n_fish)
{
  const fish_t* restrict fish = fish_;
  fish_t* restrict local_fish = local_fish_;
  int i, j;

  for (i = 0; i < n_local_fish; ++i) {

    local_fish[i].ax = 0.0;
    local_fish[i].ay = 0.0;

    for (j = 0; j < n_fish; ++j) {
      double dx, dy;

      dx = fish[j].x - local_fish[i].x;
      dy = fish[j].y - local_fish[i].y;

      if (dx != 0 || dy != 0) {
	double r2, r, a;

	r2 = f_max(dx * dx + dy * dy, min_r2);
	r = sqrt(r2);
	a = G * FISH_MASS / r2;

	local_fish[i].ax += a * dx / r;
	local_fish[i].ay += a * dy / r;
      }
    }
  }
}


/* Allocate and initialize the fish positions / velocities / accelerations. */
void
init_fish()
{
  int i;
  fish = (fish_t *) malloc(n_fish * sizeof(fish_t));
  for (i = 0; i < n_fish; ++i) {
    if (uniformp) {
      fish[i].x = unscale_coord(drand48());
      fish[i].y = unscale_coord(drand48());
    } else {
      const double angle = i * (2.0 * M_PI / n_fish);
      fish[i].x = unscale_coord(0.5 * cos(angle) + 0.5);
      fish[i].y = unscale_coord(0.5 * sin(angle) + 0.5);
    }
    fish[i].vx = fish[i].vy = fish[i].ax = fish[i].ay = 0.0;
  }
}


/* Apply reflective boundary conditions (fish bounce off walls). */
void
bounce_fish (fish_t* fish)
{
  while (fish->x < LEFT_WALL || fish->x > RIGHT_WALL) {
    if (fish->x < LEFT_WALL) {
      fish->x = 2.0 * LEFT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
    if (fish->x > RIGHT_WALL) {
      fish->x = 2.0 * RIGHT_WALL - fish->x;
      fish->vx = -fish->vx;
    }
  }
  while (fish->y < LEFT_WALL || fish->y > RIGHT_WALL) {
    if (fish->y < LEFT_WALL) {
      fish->y = 2.0 * LEFT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
    if (fish->y > RIGHT_WALL) {
      fish->y = 2.0 * RIGHT_WALL - fish->y;
      fish->vy = -fish->vy;
    }
  }
}


/* Actually move the fish. */
void
move_fish(fish_t* fish, const int n_fish, const double dt)
{
  int i;
  for (i = 0; i < n_fish; ++i) {
    fish[i].x += dt * fish[i].vx;
    fish[i].y += dt * fish[i].vy;
    fish[i].vx += dt * fish[i].ax;
    fish[i].vy += dt * fish[i].ay;
    bounce_fish(&fish[i]);
  }
}


/*
  Dump out all the fishies (and their center of gravity)
  in a format that the viewer understands.
*/
void
output_fish(FILE* output_fp, const double t, const double dt,
	    const fish_t* fish, const int n_fish)
{
  int i;
  double cg_x = 0.0;
  double cg_y = 0.0;

  fprintf(output_fp, "%.5g (%.5g):\n", t, dt);
  for (i = 0; i < n_fish; ++i) {
    cg_x += fish[i].x;
    cg_y += fish[i].y;
    fprintf(output_fp, "  %d: (%g, %g)\n", i,
	    scale_coord(fish[i].x), scale_coord(fish[i].y));
  }
  cg_x /= n_fish;
  cg_y /= n_fish;
  fprintf(output_fp, "  cg: (%g, %g)\n", scale_coord(cg_x),
	  scale_coord(cg_y));
}

