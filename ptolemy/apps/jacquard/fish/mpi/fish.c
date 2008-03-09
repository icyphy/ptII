#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include <math.h>

#include <mpi.h>

#include "config.h"
#include "fish.h"
#include "mpi-fish.h"

FILE *output_fp; /* Pointer to output file */
int outputp;

#ifdef TRACE_WITH_VAMPIR
  int tracingp;
#endif

int n_fish = 10; /* Other defaults in fish-lib.c. */
fish_t* fish; /* _All_ fish */
int n_local_fish;
fish_t* local_fish;

#define start_mpi_timer(t) do { *(t) -= MPI_Wtime(); } while (0)
#define stop_mpi_timer(t) do { *(t) += MPI_Wtime(); } while (0)


void make_fishtype (MPI_Datatype* fishtype);
#define MIN_FISH_PER_PROC 2
#define NUM_NEIGHBOR 8
void split_fish (const int n_proc, int* fish_off, int* n_fish_split);

void init_fish (const int rank, const int* fish_off, 
		const int* n_fish_split, const int row, 
		const int column, fish_t (*fishProc)[n_fish], int* n_fish_proc);
void interact_fish_mpi(fish_t* local_fish_, const int n_local_fish,
		   const fish_t* fish_, const int n_fish);
void bounce_fish (fish_t* fish);
void move_fish (fish_t* fish, const int n_fish, const double dt);
double compute_norm (const fish_t* fish, const int n_fish);
void output_fish(FILE* output_fp, const double t, const double dt,
		 const fish_t* fish, const int n_fish);


void Isend_receive_fish(fish_t** send_fish, int* n_send_fish, fish_t receive_fish[NUM_NEIGHBOR][n_fish], int n_receive_fish, MPI_Request* sendReqArray, MPI_Request* recvReqArray);
void wait_for_fish(MPI_Request* recvReqArray, int* n_fish);
void get_interacting_fish(fish_t* fish, const int n_fish, fish_t** result_fish, int* n_result_fish, const unsigned char isMigrate);


  double topBound, bottomBound, leftBound, rightBound;
  MPI_Datatype fishtype;
  const MPI_Comm comm = MPI_COMM_WORLD;
  int rank;
  int row, column;
  int n_proc;
  int iter= 0;
  int dbg = 0;


int main(int argc, char **argv) {
  
  double sum_total_timer, total_timer = 0.0;
  double sum_gather_timer, gather_timer = 0.0;
  double sum_mpi_timer, mpi_timer = 0.0;

  double curr_time;
  double output_time;
  double dt = 0.0;
  double local_max_norm = 0.1;
  double max_norm = 0;

  int steps;

  int* fish_off;
  int* n_fish_split;

  MPI_Init (&argc, &argv);

#ifdef TRACE_WITH_VAMPIR
  VT_symdef(TRACE_LOCAL_COMP, "Local computation", "Computation");
  VT_symdef(TRACE_FISH_GATHER, "Gathering to 0", "Communication");
  VT_symdef(TRACE_MAX_NORM, "Collecting max norm", "Communication");
  VT_symdef(TRACE_OUTPUT, "Output", "Output");
#endif

  MPI_Comm_size (comm, &n_proc);
  MPI_Comm_rank (comm, &rank);
  make_fishtype (&fishtype);

  get_options(argc, argv);
  srand48(clock());

  //MPI_Allreduce (&local_max_norm, &max_norm, 1, MPI_DOUBLE, MPI_MAX, comm);
  //printf("local_max_norm = %g, max_norm = %g\n", local_max_norm, max_norm);


#ifdef TRACE_WITH_VAMPIR
    VT_traceoff();
#endif

  if (output_filename) {
    outputp = 1;
    if (0 == rank) {
      output_fp = fopen(output_filename, "w");
      if (output_fp == NULL) {
	printf("Could not open %s for output\n", output_filename);
	exit(1);
      }
      fprintf(output_fp, "n_fish: %d\n", n_fish);
    }
  }

  fish_off = malloc ( (n_proc+1) * sizeof(int) );
  n_fish_split = malloc ( (n_proc) * sizeof(int) );
  
  //split each fish to different processors.
  //fish_off: offset index of the fish in that processor
  //n_fish_split is the # of fish in each processor
  //ALL FUNCTIONALITY OF split_fish SHOULD BE DONE AFTER init_fish
  //split_fish (n_proc, fish_off, n_fish_split); 
  //n_local_fish = n_fish_split[rank];

  /*
    All fish are generated on proc 0 to ensure same random numbers.
    (Yes, the circle case could be parallelized.  Feel free to
    do it.)
  */ 
  
  //split physical box sizes
  row = (int)sqrt((double)n_proc);
  column = n_proc/row;

  double rowSep = WALL_SEP/row;
  double columnSep = WALL_SEP/column;
  
  int rowIndex = rank / column;
  int columnIndex = rank % column;
  topBound = rowSep * rowIndex;
  bottomBound = topBound + rowSep;
  leftBound = columnSep * columnIndex;
  rightBound = leftBound + columnSep;

  assert(n_proc % row == 0);

  // Add n_proc # of arrays each holding ID of local fishes
  fish_t fishProc[n_proc][n_fish];
  int n_fish_proc[n_proc];
  int k;
  for (k = 0; k < n_proc; k++) n_fish_proc[k] = 0;
  //////////////////////////////////

  init_fish (rank, fish_off, n_fish_split, row, column, fishProc, n_fish_proc);

  // distribute initial conditions to all processes
  if (rank == 0) {
    local_fish = fishProc[0];
    n_local_fish = n_fish_proc[0];

    // Functionality of MPI_Scatterv is done here with Isends
    //MPI_Request request[n_proc-1];
    
	int mesTag = 0;
    MPI_Request *req;
    for (k = 1; k < n_proc; ++k) {
	//printf("n_fish_proc[%d], %d\n", k, n_fish_proc[k]);
	    MPI_Isend(fishProc[k], n_fish_proc[k], fishtype, k, mesTag, comm, req);
    }
  } else {
    MPI_Status status;
    // Processors of rank != 0 receives.
    MPI_Recv( local_fish, n_fish, fishtype, 0, MPI_ANY_TAG, comm, &status);
    MPI_Get_count(&status, fishtype, &n_local_fish);
  }
  printf("rank[%d], n_local_fish = %d\n", rank, n_local_fish);
  ///*
  //MPI_Scatterv (fish, n_fish_split, fish_off, fishtype,
  //		local_fish, n_local_fish, fishtype,
  //		0, comm);
  //*/

#ifdef TRACE_WITH_VAMPIR
    tracingp = 1;
    VT_traceon();
#endif

  start_mpi_timer(&total_timer);



  for (output_time = 0.0, curr_time = 0.0, steps = 0;
       curr_time <= end_time && steps < max_steps;
       curr_time += dt, ++steps) {

#ifdef TRACE_WITH_VAMPIR
    if (steps >= STEPS_TO_TRACE) {
      tracingp = 0; VT_traceoff();
    }
#endif

    trace_begin(TRACE_FISH_GATHER);
    start_mpi_timer (&gather_timer);
    start_mpi_timer (&mpi_timer);
    /* 
       Pull in all the fish.  Obviously, this is not a good idea.
       You will be greatly expanding this one line...

       However, feel free to waste memory when producing output.
       If you're dumping fish to a file, go ahead and do an
       Allgatherv _in the output steps_ if you want.  Or you could
       pipeline dumping the fish.
    MPI_Allgatherv (local_fish, n_local_fish, fishtype,
		    fish, n_fish_split, fish_off, fishtype, comm);
    */

    //MPI_Request* sendReq, recvReq;

    // Set aside buffer for fish received from other processes.

/*
    for (j = 0; j < NUM_NEIGHBOR; ++j) {
        //FIXME: which neighbors does not exist?
        if (rankNeighbor[j] >= 0) {
            MPI_Isend(local_fish, n_local_fish, fishtype, rankNeighbor[j], MPI_ANY_TAG, comm, &sendReqArray);
            MPI_Irecv(impact_fish, n_fish, fishtype, rankNeighbor[NUM_NEIGHBOR - j], MPI_ANY_TAG, comm, &sendReqArray);
            MPI_Wait(recvReq, MPI_STATUS_IGNORE);
            interact_fish_mpi(local_fish, n_local_fish, impact_fish, sizeof(impact_fish));
        }
    }
*/

	// get migrate fish
	// send migrate fish
	// receive migrate fish
	// update local fish
	// get impact fish
	// send impact fish
	// receive impact fish
	// interact impact fish
	// interact local fish
	// move

    MPI_Request sendReqArray[NUM_NEIGHBOR];
    MPI_Request recvReqArray[NUM_NEIGHBOR];

	fish_t receive_impact_fish[NUM_NEIGHBOR][n_fish];
	int n_receive_impact_fish[NUM_NEIGHBOR];

	fish_t receive_migrate_fish[NUM_NEIGHBOR][n_fish];
	int n_receive_migrate_fish[NUM_NEIGHBOR];


	int n_send_impact_fish[NUM_NEIGHBOR];
	fish_t* send_impact_fish[NUM_NEIGHBOR];

	int n_send_migrate_fish[NUM_NEIGHBOR];
	fish_t* send_migrate_fish[NUM_NEIGHBOR];


	get_interacting_fish( local_fish, n_local_fish, send_migrate_fish, n_send_migrate_fish, 1);

int tmp;
for (tmp = 0; tmp < NUM_NEIGHBOR; tmp++) {
	printf("rank[%d], iter[%d] ------- get [%d] migrate fish for neig[%d]. \n", rank, iter, n_send_migrate_fish[tmp], tmp);
}

	Isend_receive_fish(send_migrate_fish, n_send_migrate_fish, receive_migrate_fish, n_fish, sendReqArray, recvReqArray);
	wait_for_fish(recvReqArray, n_receive_migrate_fish);

	// FIXME: Have not implement update on local fish.
	//update_local_fish();

	get_interacting_fish(local_fish, n_local_fish, send_impact_fish, n_send_impact_fish, 0);

for (tmp = 0; tmp < NUM_NEIGHBOR; tmp++) {
	printf("rank[%d], iter[%d] ------- get [%d] impact fish for neig[%d]. \n", rank, iter, n_send_impact_fish[tmp], tmp);
}

	Isend_receive_fish(send_impact_fish, n_send_impact_fish, receive_impact_fish, n_fish, sendReqArray, recvReqArray);
	wait_for_fish(recvReqArray, n_receive_impact_fish);

	int index;
	for (index = 0; index < NUM_NEIGHBOR; index++) {
		if (n_receive_impact_fish[index] > 0) {
			interact_fish_mpi(local_fish, n_local_fish, receive_impact_fish[index], n_receive_impact_fish[index]);
		}
	}

    //*/
	// make sure we are sending and receiving the same # msg.
	//assert(dbg == 0);

    // While waiting, interact with fish in its own pocket first
printf("rank[%d], iter[%d] ------- interact [%d] local fishes\n", rank, iter, n_local_fish);
		interact_fish_mpi(local_fish, n_local_fish, local_fish, n_local_fish);
printf("rank[%d], iter[%d] ------- finished interact local fish\n", rank, iter);

    stop_mpi_timer (&gather_timer);
    stop_mpi_timer (&mpi_timer);
    trace_end(TRACE_FISH_GATHER);

    /*
      We only output once every output_interval time unit, at most.
      Without that restriction, we can easily create a huge output
      file.  Printing a record for ten fish takes about 300 bytes, so
      for every 1000 steps, we could dump 300K of info.  Now scale the
      number of fish by 1000...
     */
    trace_begin(TRACE_OUTPUT);
    if (outputp && curr_time >= output_time) {
      if (0 == rank)
		output_fish (output_fp, curr_time, dt, fish, n_fish);
		output_time = curr_time + output_interval;
    }
    trace_end(TRACE_OUTPUT);

    trace_begin (TRACE_LOCAL_COMP);
    //interact_fish (local_fish, n_local_fish, fish, n_fish);

    local_max_norm = compute_norm (local_fish, n_local_fish);
    trace_end (TRACE_LOCAL_COMP);

    trace_begin (TRACE_MAX_NORM);
    start_mpi_timer (&mpi_timer);
printf("rank[%d], iter[%d] ------- Allreduce max_norm, \n", rank, iter);
	MPI_Allreduce (&local_max_norm, &max_norm, 1, MPI_DOUBLE, MPI_MAX, comm);
printf("rank[%d], iter[%d] ------- local_max_norm: %g, max_norm: %g\n", local_max_norm, max_norm);
    stop_mpi_timer (&mpi_timer);
    trace_end (TRACE_MAX_NORM);

    trace_begin (TRACE_LOCAL_COMP);
    dt = max_norm_change / max_norm;
    dt = f_max(dt, min_dt);
    dt = f_min(dt, max_dt);

printf("rank[%d], iter[%d] ------- moving [%d] local_fish, \n", rank, iter, n_local_fish);
    move_fish(local_fish, n_local_fish, dt);
printf("rank[%d], iter[%d] ------- finished moving.\n", rank, iter);
    
    trace_end (TRACE_LOCAL_COMP);
iter++;
  }

  stop_mpi_timer(&total_timer);

#ifdef TRACE_WITH_VAMPIR
    VT_traceoff();
#endif

  if (outputp) {
    MPI_Allgatherv (local_fish, n_local_fish, fishtype,
		    fish, n_fish_split, fish_off, fishtype, comm);
    if (0 == rank) {
      output_fish (output_fp, curr_time, dt, fish, n_fish);
      printf("\tEnded at %g (%g), %d (%d) steps\n",
	     curr_time, end_time, steps, max_steps);
    }
  }

printf("rank[%d], ------- 39, \n", rank);
  MPI_Reduce (&total_timer, &sum_total_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);
printf("rank[%d], ------- 40, \n", rank);
  MPI_Reduce (&gather_timer, &sum_gather_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);
printf("rank[%d], ------- 41, \n", rank);
  MPI_Reduce (&mpi_timer, &sum_mpi_timer, 1, MPI_DOUBLE,
	      MPI_SUM, 0, comm);
printf("rank[%d], ------- 42, \n", rank);

  if (0 == rank) {
    printf("Number of PEs: %d\n"
	   "Time taken on 0: %g (avg. %g)\n"
	   "Time in gathers on 0: %g (avg %g)\n"
	   "Time in MPI on 0: %g (avg %g)\n",
	   n_proc,
	   total_timer, sum_total_timer / n_proc,
	   gather_timer, sum_gather_timer / n_proc,
	   mpi_timer, sum_mpi_timer / n_proc);
  }

printf("rank[%d], ------- 43, \n", rank);
  MPI_Barrier (comm);
printf("rank[%d], ------- 44, \n", rank);
  MPI_Finalize ();
printf("rank[%d], ------- done!!, \n", rank);
  return 0;
}

//*************************************************************************************/
////////////////////////////////////////////////////////////////////////////////////////
// Isend_receive_fish()
////////////////////////////////////////////////////////////////////////////////////////
//*************************************************************************************/
void Isend_receive_fish(fish_t** send_fish, int* n_send_fish, 
						fish_t receive_fish[NUM_NEIGHBOR][n_fish], int n_receive_fish,
						MPI_Request* sendReqArray, MPI_Request* recvReqArray) {
	int mesTag = 0;
	int rankNeighbor[NUM_NEIGHBOR];
    rankNeighbor[0] = rank - row - 1;
    rankNeighbor[1] = rank - row;
    rankNeighbor[2] = rank - row + 1;
    rankNeighbor[3] = rank - 1;
    rankNeighbor[4] = rank + 1;
    rankNeighbor[5] = rank + row - 1;
    rankNeighbor[6] = rank + row;
    rankNeighbor[7] = rank + row + 1;						
						
	assert(n_proc >= 9);
    //Index for receive_fish is the rank of the source the message's coming from.
    
	// sendrecv from neighbor 0, 7
    if (!(rank < column) && !(rank % column == 0)) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[0], \n", rank, iter, n_send_fish[0]);
        // neighbor 0
        MPI_Isend(send_fish[0], n_send_fish[0], fishtype, rankNeighbor[0], mesTag, comm, &sendReqArray[0]);
        // neighbor 7
        MPI_Irecv(receive_fish[0], n_receive_fish, fishtype, rankNeighbor[0], MPI_ANY_TAG, comm, &recvReqArray[0]);
	} else {
		recvReqArray[0] = MPI_REQUEST_NULL;
	}

    if (!(rank >= n_proc - column) && ((rank + 1) % column != 0)) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[7], \n", rank, iter, n_send_fish[7]);
        // neighbor 0
        MPI_Irecv(receive_fish[7], n_receive_fish, fishtype, rankNeighbor[7], MPI_ANY_TAG, comm, &recvReqArray[7]);
        // neighbor 7
        MPI_Isend(send_fish[7], n_send_fish[7], fishtype, rankNeighbor[7], mesTag, comm, &sendReqArray[7]);
    } else {
		recvReqArray[7] = MPI_REQUEST_NULL;
	}

    //j++;
    // sendrecv from neighbor 1, 6
    if (!(rank < column)) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[1], \n", rank, iter, n_send_fish[1]);
        // neighbor 1
        MPI_Isend(send_fish[1], n_send_fish[1], fishtype, rankNeighbor[1], mesTag, comm, &sendReqArray[1]);
        // neighbor 6
        MPI_Irecv(receive_fish[1], n_receive_fish, fishtype, rankNeighbor[1], MPI_ANY_TAG, comm, &recvReqArray[1]);
    } else {
		recvReqArray[1] = MPI_REQUEST_NULL;
	}


    if (!(rank >= n_proc - column)){
        // neighbor 1
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[6], \n", rank, iter, n_send_fish[6]);
        MPI_Irecv(receive_fish[6], n_receive_fish, fishtype, rankNeighbor[6], MPI_ANY_TAG, comm, &recvReqArray[6]);
        // neighbor 6
        MPI_Isend(send_fish[6], n_send_fish[6], fishtype, rankNeighbor[6], mesTag, comm, &sendReqArray[6]);
    } else {
		recvReqArray[6] = MPI_REQUEST_NULL;
	}


     // sendrecv from neighbor 2, 5
    if (!(rank < column) && ((rank + 1) % column != 0)) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[2], \n", rank, iter, n_send_fish[2]);
        // neighbor 2
        MPI_Isend(send_fish[2], n_send_fish[2], fishtype, rankNeighbor[2], mesTag, comm, &sendReqArray[2]);
        // neighbor 5
        MPI_Irecv(receive_fish[2], n_receive_fish, fishtype, rankNeighbor[2], MPI_ANY_TAG, comm, &recvReqArray[2]);
    } else {
		recvReqArray[2] = MPI_REQUEST_NULL;
	}


    if (!(rank >= n_proc - column) && (rank % column != 0)) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[5], \n", rank, iter, n_send_fish[5]);
        // neighbor 2
        MPI_Irecv(receive_fish[5], n_receive_fish, fishtype, rankNeighbor[5], MPI_ANY_TAG, comm, &recvReqArray[5]);
        // neighbor 5
        MPI_Isend(send_fish[5], n_send_fish[5], fishtype, rankNeighbor[5], mesTag, comm, &sendReqArray[5]);
    } else {
		recvReqArray[5] = MPI_REQUEST_NULL;
	}


    //j++;
    // sendrecv from neighbor 3, 4
    if (rank % column != 0) {
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[3], \n", rank, iter, n_send_fish[3]);
        // neighbor 3
        MPI_Isend(send_fish[3], n_send_fish[3], fishtype, rankNeighbor[3], mesTag, comm, &sendReqArray[3]);
        // neighbor 4
        MPI_Irecv(receive_fish[3], n_receive_fish, fishtype, rankNeighbor[3], MPI_ANY_TAG, comm, &recvReqArray[3]);
    } else {
		recvReqArray[3] = MPI_REQUEST_NULL;
	}


    if ((rank + 1) % column != 0){
dbg++;
printf("rank[%d], iter[%d] ------- send [%d] fish to neig[4], \n", rank, iter, n_send_fish[4]);
        // neighbor 3
        MPI_Irecv(receive_fish[4], n_receive_fish, fishtype, rankNeighbor[4], MPI_ANY_TAG, comm, &recvReqArray[4]);
        // neighbor 4
        MPI_Isend(send_fish[4], n_send_fish[4], fishtype, rankNeighbor[4], mesTag, comm, &sendReqArray[4]);
    }    else {
		recvReqArray[4] = MPI_REQUEST_NULL;
	}
}


//*************************************************************************************/
////////////////////////////////////////////////////////////////////////////////////////
// wait_for_fish()
////////////////////////////////////////////////////////////////////////////////////////
//*************************************************************************************/
void wait_for_fish(MPI_Request* recvReqArray, int* n_fish) {
    // Now wait for any recv to come back.
    int k;
    int arrayIndex;
	int numNeighbor = NUM_NEIGHBOR;
    
	if ((rank < column) || ((rank + 1) % column == 0) || (rank >= n_proc - column) || (rank % column == 0))
        numNeighbor = 5;
    if (rank == 0 || rank == column - 1 || rank == n_proc - 1 || rank == n_proc - column)
        numNeighbor = 3;

	MPI_Status statusArray[numNeighbor];

	// Zero the count array.
	for (k = 0; k < NUM_NEIGHBOR; k++) {
		n_fish[k] = 0;
	}
	
    for (k = 0; k < numNeighbor; ++k) {
        MPI_Waitany(NUM_NEIGHBOR, recvReqArray, &arrayIndex, &statusArray[k]);
		//recvReqArray[arrayIndex] = MPI_REQUEST_NULL;

		assert(arrayIndex >= 0 && arrayIndex <= 7);
		if (arrayIndex != MPI_UNDEFINED) {
dbg--;
			MPI_Get_count(&statusArray[k], fishtype, &n_fish[arrayIndex]);
		} 
	}
}


#define NEIG_TOP_LEFT       5
#define NEIG_TOP            6
#define NEIG_TOP_RIGHT      7
#define NEIG_LEFT           3
#define NEIG_RIGHT          4
#define NEIG_BOTTOM_LEFT    0
#define NEIG_BOTTOM         1
#define NEIG_BOTTOM_RIGHT   2

#define MF_NEITHER 0
#define MF_LEFT 1
#define MF_RIGHT 2

#define R_SQRT2_OVER2 0.017677669529663688110021109052621


//*************************************************************************************/
////////////////////////////////////////////////////////////////////////////////////////
// get_interacting_fish()
////////////////////////////////////////////////////////////////////////////////////////
//*************************************************************************************/
void get_interacting_fish(fish_t* fish, const int n_fish, fish_t** result_fish, int* n_result_fish, const unsigned char isMigrate) {
	  int i, j;
	  int numMF[] = {0, 0, 0, 0, 0 ,0 ,0, 0};
	  int numIF[] = {0, 0, 0, 0, 0 ,0 ,0, 0};

	  // alloc two char for each fish, the first char indicate whether there
	  // is a migration (1 == YES). Second char encode the direction (i.e.
	  // NEIG_TOP, and etc.).
	  unsigned char mFishMap[n_fish][2];   
	//unsigned char* mFishMap = (unsigned char[][]) realloc(mFishMap, sizeof(unsigned char) * n_fish * 2);

	  // An array of 8 booleans for each fish to mark whether it is a 
	  // impact fish for the 8 neighbors. It can be a impact fish to
	  // multiple neighbors.
	  unsigned char iFishMap[n_fish][8];
	//unsigned char* iFishMap = (unsigned char*) realloc(iFishMap, sizeof(unsigned char) * n_fish * 8);

	// initialize.
	for (i = 0; i < n_fish; i++) {
		mFishMap[i][0] = 0;
		mFishMap[i][1] = 0;
		for (j = 0; j < 8; j++) {
			iFishMap[i][j] = 0;
		}
	}
	  
	  for (i = 0; i < n_fish; ++i) {
	    /** Given:  double topBound;
	     *          double bottomBound;
	     *          double leftBound;
	     *          double rightBound;
	     */

		// Send interacting fishes (IF).
	    // Send migrate fishes (MF).
	    
	    if (fish[i].x > (rightBound - 0.025)) {
	        if (fish[i].x > rightBound) {
				// MF.
	            mFishMap[i][0] = 1;
	            mFishMap[i][1] = NEIG_RIGHT;
	            numMF[NEIG_RIGHT]++;
	        } else {
	            // IF.
	            iFishMap[i][NEIG_RIGHT] = 1;
	            numIF[NEIG_RIGHT]++;
	        }
	    } else if (fish[i].x < (leftBound + 0.025)) {
	        if (fish[i].x < leftBound) {
				// MF.
	            mFishMap[i][0] = 1;
	            mFishMap[i][1] = NEIG_LEFT;
	            numMF[NEIG_LEFT]++;
	        } else {
	            // IF.
	            iFishMap[i][NEIG_LEFT] = 1;
	            numIF[NEIG_LEFT]++;
	        }
	    } 
	    
	    if (fish[i].y > (topBound - 0.025)) {
	        if (fish[i].y > topBound) {
				// MF.
	            if (mFishMap[i][0] == 1) {
	                if (mFishMap[i][1] == NEIG_RIGHT) {
	                    mFishMap[i][1] = NEIG_TOP_RIGHT;
	                    numMF[NEIG_RIGHT]--;
	                    numMF[NEIG_TOP_RIGHT]++;
	                } else {//if (mFishMap[i][1] == NEIG_LEFT) {
	                    mFishMap[i][1] = NEIG_TOP_LEFT;
	                    numMF[NEIG_LEFT]--;
	                    numMF[NEIG_TOP_LEFT]++;
	                }
	            } else {
	                mFishMap[i][0] = 1;
	                mFishMap[i][1] = NEIG_TOP;
	                numMF[NEIG_TOP]++;
	            }
	        } else {
	            // IF.
	            iFishMap[i][NEIG_TOP] = 1;
	            numIF[NEIG_TOP]++;
	            
	            if (iFishMap[i][NEIG_RIGHT] == 1) {
	                // Check TOP_RIGHT
	                if ((fish[i].x + R_SQRT2_OVER2 > rightBound) && (fish[i].y + R_SQRT2_OVER2 > topBound)) {
	                    iFishMap[i][NEIG_TOP_RIGHT] = 1; 
	                    numIF[NEIG_TOP_RIGHT]++;
	                }
	            } else if (iFishMap[i][NEIG_LEFT] == 1) {
	                // Check TOP_LEFT
	                if ((fish[i].x - R_SQRT2_OVER2 < leftBound) && (fish[i].y + R_SQRT2_OVER2 > topBound)) {
	                    iFishMap[i][NEIG_TOP_LEFT] = 1; 
	                    numIF[NEIG_TOP_LEFT]++;
	                }
	            }
	        }
	    } else if (fish[i].y < (bottomBound + 0.025)) {
	        if (fish[i].y < bottomBound) {
				// MF.
	            if (mFishMap[i][0] == 1) {
	                if (mFishMap[i][1] == NEIG_RIGHT) {
	                    mFishMap[i][1] = NEIG_BOTTOM_RIGHT;
	                    numMF[NEIG_RIGHT]--;
	                    numMF[NEIG_BOTTOM_RIGHT]++;
	                } else {
	                    mFishMap[i][1] = NEIG_BOTTOM_LEFT;
	                    numMF[NEIG_LEFT]--;
	                    numMF[NEIG_BOTTOM_LEFT]++;
	                }
	            } else {
	                mFishMap[i][0] = 1;
	                mFishMap[i][1] = NEIG_BOTTOM;
	                numMF[NEIG_BOTTOM]++;
	            }
	        } else {
	            // IF.
	            iFishMap[i][NEIG_BOTTOM] = 1;
	            numIF[NEIG_BOTTOM]++;
	            
	            if (iFishMap[i][NEIG_RIGHT] == 1) {
	                // Check BOTTOM_RIGHT
	                if ((fish[i].x + R_SQRT2_OVER2 > rightBound) && (fish[i].y - R_SQRT2_OVER2 < bottomBound)) {
	                    iFishMap[i][NEIG_BOTTOM_RIGHT] = 1; 
	                    numIF[NEIG_BOTTOM_RIGHT]++;
	                }
	            } else if (iFishMap[i][NEIG_LEFT] == 1) {
	                // Check BOTTOM_LEFT
	                if ((fish[i].x - R_SQRT2_OVER2 < leftBound) && (fish[i].y - R_SQRT2_OVER2 < bottomBound)) {
	                    iFishMap[i][NEIG_BOTTOM_LEFT] = 1; 
	                    numIF[NEIG_BOTTOM_LEFT]++;
	                }
	            }
	        }
	    }
	  }

	  // Create the arrays to send to neighbors.  
	  fish_t* interactingFish[8];
	  fish_t* migrateFish[8];
	  for (j = 0; j < 8; j++) {
	      interactingFish[j] = (fish_t*) malloc(numIF[j] * sizeof(fish_t));
	      migrateFish[j] = (fish_t*) malloc(numMF[j] * sizeof(fish_t));
	  }
	    
	  int icounter[8] = {0};
	  int mcounter[8] = {0};
	  for (i = 0; i < n_fish; i++) {
	      for (j = 0; j < 8; j++) {
	          if (iFishMap[i][j] == 1) {
	              printf("Interacting fish[%g, %g] with neig_proc[%d]. Right=%g, Left=%g, Top=%g, Bottom=%g.", fish[i].x, fish[i].y, j, rightBound, leftBound, topBound, bottomBound);
	              interactingFish[j][icounter[j]++] = fish[i]; 
	          }
	      }
	      if (mFishMap[i][0] == 1) {
	          assert(!(fish[i].x >= leftBound && fish[i].x <= rightBound && fish[i].y >= bottomBound && fish[i].y <= topBound));
	          //printf("migrate fish[%g, %g] to neig_proc[%d]. Right=%g, Left=%g, Top=%g, Bottom=%g.", fish[i].x, fish[i].y, j, rightBound, leftBound, topBound, bottomBound);

	          // mFishMap[i][1] tells you which neighbor.
	          migrateFish[mFishMap[i][1]][mcounter[mFishMap[i][1]]++] = fish[i]; 
	      }
	  }

	  for (j = 0; j < 8; j++) {
		  if (isMigrate) {
		      n_result_fish[j] = numMF[j];
		      result_fish[j] = migrateFish[j];
		  } else {
		      n_result_fish[j] = numIF[j];
		      result_fish[j] = interactingFish[j];
		  }
printf("rank[%d] ------- get_interacting_fish returns [%d] impact fish to neig[%d], \n", rank, numIF[j], j);
printf("rank[%d] ------- get_interacting_fish returns [%d] migrate fish to neig[%d], \n", rank, numMF[j], j);
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
interact_fish_mpi(fish_t* local_fish_, const int n_local_fish,
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

	r2 = dx * dx + dy * dy;
	r = sqrt(r2);
	a = (r > 1/40) ? 0 : (1.0/r) - (1.0/40/r2);

	local_fish[i].ax += a * dx / r;
	local_fish[i].ay += a * dy / r;
      }
    }
  }
}


/* Allocate and initialize the fish positions / velocities / accelerations. */
void
// FIXME: delete unwanted arguments
init_fish (const int rank,
	   const int* fish_off, const int* n_fish_split, const int row, const int column, fish_t (*fishProc)[n_fish], int* n_fish_proc)
{  
  int i, li;
  fish = malloc(n_fish * sizeof(fish_t));
  local_fish = malloc(n_fish * sizeof(fish_t));
  if (0 == rank) {
 
  // Calculate row and column distances 
  double rowSep = WALL_SEP/row;
  double columnSep = WALL_SEP/column;
  assert(columnSep >= 1/40);
 
   for (i = 0; i < n_fish; ++i) {
      if (uniformp) {
	fish[i].x = unscale_coord(drand48());
	fish[i].y = unscale_coord(drand48());
      } else {
        // this circle case could be parallelized
	    const double angle = i * (2.0 * M_PI / n_fish);
	    fish[i].x = unscale_coord(0.5 * cos(angle) + 0.5);
	    fish[i].y = unscale_coord(0.5 * sin(angle) + 0.5);
      }
      fish[i].vx = fish[i].vy = 0.0;
      fish[i].ax = fish[i].ay = 0.0;
      
//printf("init_fish 1, %d\n", rank);
      // which processor should this fish belong to?
      int xnum = (int) ((fish[i].x - LEFT_WALL) / columnSep);
      int ynum = (int) ((fish[i].y - LEFT_WALL) / rowSep);
      if (fish[i].x == RIGHT_WALL)
	 xnum = row - 1;
      if (fish[i].y == RIGHT_WALL)
         ynum = column - 1;
      int fishInRank = xnum + ynum * row;
//printf("fish[i].x, fish[i].y, columnSep, rowSep, %f, %f, %f, %f\n", fish[i].x, fish[i].y, columnSep, rowSep);
//printf("xnum, ynum, fishInRank, %d, %d, %d\n", xnum, ynum, fishInRank);
      fishProc[fishInRank][n_fish_proc[fishInRank]] = fish[i];
      n_fish_proc[fishInRank]++;
      ///////////////////////////////////////////////
    }
/*
int ii;
for (ii = 0; ii < 9; ii++)
printf("n_fish_proc[%d], %d\n", ii, n_fish_proc[ii]);
*/
  }
}


/* Apply reflective boundary conditions (fish bounce off walls). */
void
bounce_fish (fish_t* fish)
{

  // FIXME: for blocks in the middle, this check is not needed.
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

  // If the fish no longer belong to the current block, send it to where it
  // belongs
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

    assert(scale_coord(fish[i].x) >= 0.0);
    assert(scale_coord(fish[i].y) >= 0.0);
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


void
make_fishtype (MPI_Datatype* fishtype)
{
  int err, i;

  /* QQQ: How does the data type affect performance? */
#if 1
  MPI_Aint disp[8];
  MPI_Datatype types[8] = { MPI_LB, MPI_DOUBLE, MPI_DOUBLE, MPI_DOUBLE,
			    MPI_DOUBLE, MPI_DOUBLE, MPI_DOUBLE, MPI_UB };
  int blocklen[8] = { 1, 1, 1, 1, 1, 1, 1, 1 };
  fish_t example[2];

  MPI_Address (&example[0], &disp[0]);
  MPI_Address (&example[0].x, &disp[1]);
  MPI_Address (&example[0].y, &disp[2]);
  MPI_Address (&example[0].vx, &disp[3]);
  MPI_Address (&example[0].vy, &disp[4]);
  MPI_Address (&example[0].ax, &disp[5]);
  MPI_Address (&example[0].ay, &disp[6]);
  MPI_Address (&example[1], &disp[7]);
  for (i = 7; i >= 0; --i) disp[i] -= disp[0];

  err = MPI_Type_struct (8, &blocklen[0], &disp[0], &types[0], fishtype);
#elif 0
  MPI_Aint disp[2];
  MPI_Aint base;
  MPI_Datatype types[2] = { MPI_DOUBLE, MPI_UB };
  int blocklen[2] = { 6, 1 };
  fish_t example[2];

  MPI_Address (&example[0], &base);
  MPI_Address (&example[0].x, &disp[0]);
  MPI_Address (&example[1], &disp[1]);
  disp[0] -= base;
  disp[1] -= base;
  err = MPI_Type_struct (2, blocklen, disp, types, fishtype);
#else
  err = MPI_Type_contiguous (6, MPI_DOUBLE, fishtype);
#endif

  if (err) {
    fprintf (stderr, "Error creating type: %d\n", err);
    MPI_Abort (MPI_COMM_WORLD, -29);
  }
  MPI_Type_commit (fishtype);
}


void
split_fish (const int n_proc, int* off, int* n)
{
  int n_per;
  int idx, num, p;

  n_per = (n_fish + n_proc - 1) / n_proc;
  if (n_per < MIN_FISH_PER_PROC) n_per = MIN_FISH_PER_PROC;

  idx = 0;
  num = n_per;
  for (p = 0; p < n_proc; ++p) {
    if (idx + num > n_fish)
      num = n_fish - idx;

    n[p] = num;
    off[p] = idx;
    idx += num;
  }
  off[p] = idx;
}
