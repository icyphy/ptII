/**
 * tvmemul.c
 * Entry source file for TinyVM emulator.
 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "specialclasses.h"
#include "specialsignatures.h"
#include "language.h"
#include "memory.h"
#include "interpreter.h"
#include "exceptions.h"
#include "load.h"
#include "trace.h"
#include "platform_hooks.h"

#define MEMORY_SIZE 8192 /* 16 Kb */
#define DEBUG_RUNS  0

Thread   *bootThread;
TWOBYTES *gMemory;
TWOBYTES gMemorySize = MEMORY_SIZE;
struct timeval gStart;

void handle_uncaught_exception (Object *exception,
                                       const Thread *thread,
				       const MethodRecord *methodRecord,
				       const MethodRecord *rootMethod,
				       byte *pc)
{
    printf ("*** UNCAUGHT EXCEPTION/ERROR: \n");
    printf ("--  Exception class   : %u\n", (unsigned) get_class_index (exception));
    printf ("--  Thread            : %u\n", (unsigned) thread->threadId);
    printf ("--  Method signature  : %u\n", (unsigned) methodRecord->signatureId);
    printf ("--  Root method sig.  : %u\n", (unsigned) rootMethod->signatureId);
    printf ("--  Bytecode offset   : %u\n", (unsigned) pc - 
            (int) get_code_ptr(methodRecord));  					       
}

void switch_thread_hook()
{
  // NOP
}

FOURBYTES get_sys_time_impl()
{
  struct timeval now;
  FOURBYTES sysTime;
  
  if (gettimeofday(&now, NULL)) 
    perror("systime_init: gettimeofday");
  sysTime = (now.tv_sec  - gStart.tv_sec ) * 1000;
  sysTime += (now.tv_usec - gStart.tv_usec) / 1000;
  return sysTime;	
}

void run(void)
{
  #if DEBUG_RUNS
  int count = 0;
  #endif

  // Initialize binary image state
  initialize_binary();
  // Initialize memory
  gMemory = (TWOBYTES *) malloc (gMemorySize * sizeof (TWOBYTES));
  init_memory (gMemory, gMemorySize);
  // Initialize exceptions
  init_exceptions();
  // Create the boot thread (bootThread is a special global)
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);
  
  #if DEBUG_THREADS
  printf ("Created bootThread: %d. Initializing...\n", (int) bootThread);
  #endif
  
  #if DEBUG_RUNS
  for (count = 0; count < 100; count++)
  {
  #endif DEBUG_RUNS

  #if DEBUG_RCX_MEMORY
  {
    TWOBYTES numNodes, biggest, freeMem;	
    scan_memory (&numNodes, &biggest, &freeMem);
    printf ("nodes = %d\n", (int) numNodes);
    printf ("biggest = %d\n", (int) biggest);
    printf ("freeMem = %d\n", (int) freeMem);
  }
  #endif

  init_threads();
  if (!init_thread (bootThread))
  {
    printf ("Unable to initialize threading module.\n");
    exit (1);	  
  }
  // Execute the bytecode interpreter
  engine();
  // Engine returns when all non-daemon threads are dead
  #if DEBUG_STARTUP
  printf ("Engine finished.\n");
  #endif

  #if DEBUG_RUNS
  }
  #endif DEBUG_RUNS
}

int main (int argc, char *argv[])
{
  if (argc != 2)
  {
    printf ("%s runs a binary dumped by the linker.\n", argv[0]);
    printf ("Use: %s <path>\n", argv[0]);
    exit (1);
  }
  #if DEBUG_STARTUP
  printf ("Reading binary %s\n", argv[1]);
  #endif
  readBinary (argv[1]);
  if (gettimeofday(&gStart, NULL)) 
    perror("main: gettimeofday");
  run();
  return 0;
} 



