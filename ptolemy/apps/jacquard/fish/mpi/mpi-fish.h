/* Header file for MPI fish */

#if !defined(MPI_FISH_H_)
#define MPI_FISH_H_

/* --- VAMPIR setup --- */

/* Names of program phases */
#define TRACE_LOCAL_COMP  0
#define TRACE_FISH_GATHER 1
#define TRACE_MAX_NORM    2
#define TRACE_OUTPUT      1000

/* Number of time steps to trace (don't want too much output)  */
#define STEPS_TO_TRACE 10

/* Some helper macros to start and stop tracing  */
#ifdef TRACE_WITH_VAMPIR
#  include <VT.h>
#  define trace_begin(x) do { if (tracingp) VT_begin(x); } while (0)
#  define trace_end(x)   do { if (tracingp) VT_end(x);   } while (0)
#else
#  define trace_begin(x)
#  define trace_end(x)
#endif

/* --- Ensure we have standard macros --- */

#if defined(_CRAY)
#include <fp.h>
/*
  C99 defines int finite(double x).  Returns true if x is a finite
  number.  Returns false if x is infinite or NaN.
*/
#define finite(x) isfinite(x)
#endif


/* --- Define an MPI-friendly assert variant --- */

#define mpi_assert(pred)						\
  do {									\
    if (!(pred)) {							\
      int rank;								\
      MPI_Comm_rank (MPI_COMM_WORLD, &rank);				\
      fprintf(stderr, "Failed assertion at %d in %s, rank %d\n",	\
	      __LINE__, __FILE__, rank);				\
      MPI_Abort(MPI_COMM_WORLD, 1);					\
    }									\
  } while (0)

#endif /* MPI_FISH_H_ */
