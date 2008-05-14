/*** preinitBlock ***/
	/* MPI preinitialize variables*/
	MPI_Comm comm;
	int n_proc;
	int rank;
/**/


/*** timerSharedBlock ***/
	#include <sys/time.h>
	#include <time.h>
	struct Timer {
		struct timeval clock_holder;
		struct timeval duration;
	};
	
	void initialize_timer (struct Timer * t) {
		t->clock_holder.tv_sec = 0;
		t->clock_holder.tv_usec = 0;
		t->duration.tv_sec = 0;
		t->duration.tv_usec = 0;
	}
	
	void start_timer(struct Timer * t) {
		gettimeofday (&t->clock_holder, NULL);
	}
	
	void stop_timer(struct Timer * t) {
		struct timeval end_tv;
		gettimeofday (&end_tv, NULL);
		t->duration.tv_sec += (end_tv.tv_sec - t->clock_holder.tv_sec);
		t->duration.tv_usec += (end_tv.tv_usec - t->clock_holder.tv_usec);
	}
	
	double timer_duration(const struct Timer t) {
		return t.duration.tv_sec + 1.0e-6 * (double)t.duration.tv_usec;
	}
	
	struct Timer total_timer;
/**/

/*** declareMpiBufferHeader($name, $dirHeader, $capacity, $id) ***/
	// $MPI_declareStruct() is required.	
	static struct mpiBufferHeader $name = {
			0,                              		// current
			$capacity,                         		// available
			$id
	};
/**/

/*** declareLocalBufferHeader($name, $dirHeader, $capacity, $id) ***/
// $MPI_declareStruct() is required.	
struct mpiLocalBufferHeader $name = {
		0,                              		// readOffset
		0,		                         		// writeOffset
		$id
};
/**/

/*** initBlock($directorHeader) ***/

/**/


/*** initTimer ***/
	initialize_timer(&total_timer);
	start_timer(&total_timer);
/**/


/*** mpiInit ***/    
MPI_Init(&argc, &argv);
comm = MPI_COMM_WORLD;
MPI_Comm_size (comm, &n_proc);
MPI_Comm_rank (comm, &rank);
/**/


/*** initBuffer($buffer) ***/

/**/


/*** wrapupBlock() ***/
	while(1);
/**/

/*** destroyBuffer($buffer) ***/

/**/

/*** updateInputOffset ***/

/**/