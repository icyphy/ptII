
#include "classes.h"
#include "language.h"
#include "constants.h"
#include "trace.h"

#ifndef _THREADS_H
#define _THREADS_H

#define DEAD          0 /* Must be zero; see java.lang.Thread#isAlive */
#define STARTED       1
#define RUNNING       2
#define MON_WAITING   3
#define SLEEPING      4

#define SF_SIZE (sizeof(StackFrame))

extern Thread *currentThread;
extern Thread *bootThread;
extern byte gThreadCounter;
extern byte gProgramNumber;
extern boolean gRequestSuicide;

typedef struct S_StackFrame
{
  MethodRecord *methodRecord;
  Object *monitor;
  // The following 2 fields are constant for a given stack frame.
  STACKWORD *localsBase;
  boolean *isReferenceBase;
  // The following fields only need to be assigned to on switch_thread.
  byte *pc;
  STACKWORD *stackTop;
  boolean *isReference;
} StackFrame;

extern boolean init_thread (Thread *thread);
extern StackFrame *current_stackframe();
extern void enter_monitor (Object* obj);
extern void exit_monitor (Object* obj);
extern boolean switch_thread();

#define stackframe_array_ptr()   (word2ptr(currentThread->stackFrameArray))
#define stack_array_ptr()        (word2ptr(currentThread->stackArray))
#define is_reference_array_ptr() (word2ptr(currentThread->isReferenceArray))
#define stackframe_array()       ((StackFrame *) ((byte *) stackframe_array_ptr() + HEADER_SIZE))
#define stack_array()            ((STACKWORD *) ((byte *) stack_array_ptr() + HEADER_SIZE))
#define is_reference_array()     ((JBYTE *) ((byte *) is_reference_array_ptr() + HEADER_SIZE))
#define set_program_number(N_)   {gProgramNumber = (N_);}
#define inc_program_number()     {if (++gProgramNumber >= get_num_entry_classes()) gProgramNumber = 0;}
#define get_program_number()     gProgramNumber 

static inline void init_threads()
{
  gThreadCounter = 0;
  currentThread = JNULL;	
}

/**
 * Sets thread state to SLEEPING.
 * Thread should be switched immediately after calling this method.
 */
static inline void sleep_thread (const FOURBYTES time)
{
  #ifdef VERIFY
  assert (currentThread != JNULL, THREADS3);
  assert (currentThread->state != MON_WAITING, THREADS9);
  #endif

  currentThread->state = SLEEPING;
  currentThread->waitingOn = get_sys_time() + time; 	
}

#endif



