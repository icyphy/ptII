
#include "types.h"
#include "trace.h"
#include "platform_hooks.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "memory.h"
#include "exceptions.h"
#include "stack.h"

#define NO_OWNER 0x00

#define get_stack_frame() ((StackFrame *) (currentThread->currentStackFrame))

/**
 * Thread currently being executed by engine().
 */
Thread* currentThread;
byte gThreadCounter;
byte gProgramNumber;

StackFrame *current_stackframe()
{
  byte arraySize;

  arraySize = currentThread->stackFrameArraySize;
  if (arraySize == 0)
    return null;
  return stackframe_array() + (arraySize - 1);
}

inline byte get_thread_id (Object *obj)
{
  return (byte) ((obj->syncInfo & THREAD_MASK) >> THREAD_SHIFT);
}

inline void set_thread_id (Object *obj, byte threadId)
{
  obj->syncInfo = (obj->syncInfo & ~THREAD_MASK) | 
                  ((TWOBYTES) threadId << THREAD_SHIFT);
}

inline void inc_monitor_count (Object *obj)
{
  obj->syncInfo = (obj->syncInfo & ~COUNT_MASK) | 
                   ((obj->syncInfo & COUNT_MASK) + 1);
}

inline void set_monitor_count (Object *obj, byte count)
{
  obj->syncInfo = (obj->syncInfo & ~COUNT_MASK) | count;
}

boolean init_thread (Thread *thread)
{
  thread->threadId = gThreadCounter + 1;
  if (thread->state != DEAD || thread->threadId == NO_OWNER)
  {
    // Thread already initialized?
    // This assumes object creation sets state field to zero (DEAD).
    throw_exception (outOfMemoryError);
    return false;
  }
  thread->stackFrameArray = ptr2word (new_primitive_array (T_STACKFRAME, MAX_STACK_FRAMES));
  if (thread->stackFrameArray == JNULL)
    return false;
  thread->stackArray = ptr2word (new_primitive_array (T_INT, STACK_SIZE));
  if (thread->stackArray == JNULL)
  {
    free_array (ref2obj(thread->stackFrameArray));
    thread->stackFrameArray = JNULL;
    return false;    
  }
  thread->isReferenceArray = ptr2word (new_primitive_array (T_BOOLEAN, STACK_SIZE));
  if (thread->isReferenceArray == JNULL)
  {
    free_array (ref2obj(thread->stackFrameArray));
    free_array (ref2obj(thread->stackArray));
    thread->stackFrameArray = JNULL;
    thread->stackArray = JNULL;
    return false;
  }
  gThreadCounter++;
  
  #ifdef VERIFY
  assert (is_array (word2obj (thread->stackFrameArray)), THREADS0);
  assert (is_array (word2obj (thread->stackArray)), THREADS1);
  assert (is_array (word2obj (thread->isReferenceArray)), THREADS2);
  #endif

  thread->stackFrameArraySize = 0;
  thread->state = STARTED;
  if (currentThread == null)
  {
    currentThread = thread;
    #if DEBUG_THREADS
    printf ("First-time init of currentThread: %d\n", (int) currentThread);
    printf ("currentThread->state: %d\n", (int) currentThread->state);
    #endif
  }
  else
  {
    thread->nextThread = currentThread->nextThread;
  }
  currentThread->nextThread = ptr2word (thread);
  return true;
}

/**
 * Switches to next thread.
 * @return false iff there are no live threads
 *         to switch to.
 */
boolean switch_thread()
{
  Thread *anchorThread;
  Thread *previousThread;
  StackFrame *stackFrame;
  boolean liveThreadExists;

  #if DEBUG_THREADS || DEBUG_BYTECODE
  printf ("------ switch_thread: currentThread at %d\n", (int) currentThread);
  #endif

  #ifdef VERIFY
  assert (currentThread != null, THREADS4);
  #endif
  
  anchorThread = currentThread;
  liveThreadExists = false;
  // Save context information
  stackFrame = current_stackframe();

  #if DEBUG_THREADS
  printf ("switchThread: current stack frame: %d\n", (int) stackFrame);
  #endif
  
  #ifdef VERIFY
  assert (stackFrame != null || currentThread->state == STARTED,
          THREADS5);
  #endif

  if (stackFrame != null)
  {
    update_stack_frame (stackFrame);
  }

  // Loop until a RUNNING frame is found
 LABEL_TASKLOOP:
  #if DEBUG_THREADS
  printf ("Calling switch_thread_hook\n");
  #endif
  switch_thread_hook();
  if (gMakeRequest && gRequestCode == REQUEST_EXIT)
    return false;
  previousThread = currentThread;
  currentThread = (Thread *) word2ptr (currentThread->nextThread);
  #if DEBUG_THREADS
  printf ("Checking state of thread %d: %d\n", (int) currentThread, (int) currentThread->state);
  #endif
  switch (currentThread->state)
  {
    case MON_WAITING:
      #ifdef VERIFY
      assert (currentThread->waitingOn != JNULL, THREADS6);
      #endif

      if (get_thread_id (word2obj (currentThread->waitingOn)) == NO_OWNER)
      {
        // NOW enter the monitor (guaranteed to succeed)
        enter_monitor (word2obj (currentThread->waitingOn));
        // Let the thread run.
        currentThread->state = RUNNING;
        #ifdef SAFE
        currentThread->waitingOn = JNULL;
        #endif
      }
      break;
    case SLEEPING:
      if (get_sys_time() >= (FOURBYTES) currentThread->waitingOn)
      {
	currentThread->state = RUNNING;
	#ifdef SAFE
	currentThread->waitingOn = JNULL;
	#endif SAFE
      }
      break;
    case DEAD:

      #if REMOVE_DEAD_THREADS
      // This order of deallocation is actually crucial to avoid leaks
      free_array ((Object *) word2ptr (currentThread->isReferenceArray));
      free_array ((Object *) word2ptr (currentThread->stackArray));
      free_array ((Object *) word2ptr (currentThread->stackFrameArray));

      #ifdef SAFE
      currentThread->stackFrameArray = JNULL;
      currentThread->stackArray = JNULL;
      currentThread->isReferenceArray = JNULL;
      #endif SAFE
      #endif REMOVE_DEAD_THREADS
      
      if (currentThread == anchorThread)
      {
	if (!liveThreadExists)
	{
          #if DEBUG_THREADS
          printf ("switch_thread: all threads are dead: %d\n", (int) currentThread);
          #endif
	  currentThread = null;
          return false;
	}
	/* anchorThread should always point somewhere in the circular list */
	anchorThread = previousThread;
      }

      /* Remove currentThread from circular list */
      
      previousThread->nextThread = ptr2word (currentThread->nextThread);
      currentThread = previousThread;
      
      break;
    case STARTED:      
      // Put stack ptr at the beginning of the stack so we can push arguments
      // to entry methods. This assumes set_top_word or set_top_ref will
      // be called immediately below.
      init_stack_ptr_and_push_void();
      currentThread->state = RUNNING;
      if (currentThread == bootThread)
      {
        ClassRecord *classRecord;

        classRecord = get_class_record (get_entry_class (gProgramNumber));
        // Initialize top word with fake parameter for main():
        set_top_ref (JNULL);
        // Push stack frame for main method:
        dispatch_special (classRecord, find_method (classRecord, MAIN_V), null);
        // Push another if necessary for the static initializer:
        dispatch_static_initializer (classRecord, pc);
      }
      else
      {
        set_top_ref (ptr2word (currentThread));
        dispatch_virtual ((Object *) currentThread, RUN_V, null);
      }
      // The following is needed because the current stack frame
      // was just created
      stackFrame = current_stackframe();
      update_stack_frame (stackFrame);
      break;
  }
  #if DEBUG_THREADS
  printf ("switch_thread: done processing thread %d: %d\n", (int) currentThread,
          (int) (currentThread->state == RUNNING));
  #endif

  if (currentThread->state != RUNNING)
  {
    if (currentThread->state != DEAD)
      liveThreadExists = true;
    goto LABEL_TASKLOOP;
  }

  #if DEBUG_THREADS
  printf ("getting current stack frame...\n");
  #endif

  stackFrame = current_stackframe();

  #if DEBUG_THREADS
  printf ("updading registers...\n");
  #endif

  update_registers (stackFrame);

  #if DEBUG_THREADS
  printf ("done updading registers\n");
  #endif
  
//  if (gRequestSuicide)
//    throw_exception (threadDeath);

  return true;
}

/**
 * currentThread enters obj's monitor.
 * Note that this operation is atomic as far as the program is concerned.
 */
void enter_monitor (Object* obj)
{
  byte owner;
  byte tid;

  if (obj == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }
  owner = get_thread_id (obj);
  tid = currentThread->threadId;
  if (owner != NO_OWNER && tid != owner)
  {
    // Make thread wait until the monitor is relinquished.
    currentThread->state = MON_WAITING;
    currentThread->waitingOn = ptr2word (obj);
    // Gotta yield
    schedule_request (REQUEST_SWITCH_THREAD);    
    return;
  }
  set_thread_id (obj, tid);
  inc_monitor_count (obj);
}

void exit_monitor (Object* obj)
{
  byte newMonitorCount;

  if (obj == JNULL)
  {
    // Exiting due to a NPE on monitor_enter [FIX THIS]
    return;
  }

  #ifdef VERIFY
  assert (get_thread_id(obj) == currentThread->threadId, THREADS7);
  assert (get_monitor_count(obj) > 0, THREADS8);
  #endif

  newMonitorCount = get_monitor_count(obj)-1;
  if (newMonitorCount == 0)
    set_thread_id (obj, NO_OWNER);
  set_monitor_count (obj, newMonitorCount);
}





