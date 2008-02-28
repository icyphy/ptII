#if !defined(FISH_H_)
#define FISH_H_

#include "config.h"

/* For struct Timer routines. */
#include <sys/time.h>
#include <time.h>

#if defined(HAVE_ISNAN)||defined(HAVE_FMAX)
#include <math.h>
#endif

/* Structure to represent a fish state
 */
typedef struct {
  double x;
  double y;
  double vx;
  double vy;
  double ax;
  double ay;
} fish_t;

#define FISH_MASS ((double) 1.0)

#define LEFT_WALL ((double) (1.0))
#define RIGHT_WALL ((double) (2.0))
#define WALL_SEP (RIGHT_WALL - LEFT_WALL)

/* --- Options ----------------------------------------------------- */

extern int n_fish; /* Number of fish */

extern double max_norm_change; /* Maximum allowable norm change / step */
extern double max_dt; /* Max allowed time step */
extern double min_dt; /* Min allowed time step */
extern double end_time; /* Ending time for the simulation */
extern int max_steps; /* Maximum number of steps */

extern double min_r; /* Cutoff distance for attraction computation */
extern double min_r2; /* min_r squared */
extern double G; /* Gravitational constant */

extern double output_interval; /* Min time between outputs */
extern char *output_filename; /* File to receive results */

extern int uniformp; /* Flag if initial configuration is unif. distributed. */

#define STRINGARG_MAXLEN 1024
typedef enum { NULLARG, INTARG, DOUBLEARG, STRINGARG } arg_type_t;

struct arginfo {
  char c;
  arg_type_t type;
  void* val;
  const char* desc;
};

extern struct arginfo* ext_args;

/* --- Useful functions -------------------------------------------- */

/* The C99 fmax and fmin functions aren't defined on many platforms. */
#ifdef _AIX 
#define inline __inline
#endif

static inline double
f_max (double a, double b)
{
  if (a > b) return a;
  if (b > a) return b;
#if defined(HAVE_ISNAN)
  if (isnan(a)) return b;
#else
  if (a != a) return b;
#endif
  return a;
}

static inline double
f_min (double a, double b)
{
  if (a < b) return a;
  if (b < a) return b;
#if defined(HAVE_ISNAN)
  if (isnan(a)) return b;
#else
  if (a != a) return b;
#endif
  return a;
}

/* --- fish-lib ---------------------------------------------------- */

/* Process the command line options */
void get_options(int, char **);

double scale_coord(double);
double unscale_coord(double);

/* A simple wrapper around the clock() timer */
struct Timer {
  struct timeval clock_holder;
  struct timeval duration;
};

void initialize_timer (struct Timer* t);
void start_timer (struct Timer* t);
void stop_timer (struct Timer* t);
double timer_duration(const struct Timer t);

#endif /* FISH_H_ */
