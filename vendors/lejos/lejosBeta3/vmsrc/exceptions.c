
#include "types.h"
#include "trace.h"
#include "threads.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "exceptions.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "memory.h"
#include "stack.h"
#include "platform_hooks.h"

Object *outOfMemoryError;
Object *noSuchMethodError;
Object *stackOverflowError;
Object *nullPointerException;
Object *classCastException;
Object *arithmeticException;
Object *arrayIndexOutOfBoundsException;
//Object *threadDeath;

// Temporary globals:

static TWOBYTES tempCurrentOffset;
static MethodRecord *tempMethodRecord = null;
static StackFrame *tempStackFrame;
static ExceptionRecord *gExceptionRecord;
static byte gNumExceptionHandlers;
static MethodRecord *gExcepMethodRec = null;
static byte *gExceptionPc;

void init_exceptions()
{
  outOfMemoryError = new_object_for_class (JAVA_LANG_OUTOFMEMORYERROR);
  noSuchMethodError = new_object_for_class (JAVA_LANG_NOSUCHMETHODERROR);
  stackOverflowError = new_object_for_class (JAVA_LANG_STACKOVERFLOWERROR);
  nullPointerException = new_object_for_class (JAVA_LANG_NULLPOINTEREXCEPTION);
  classCastException = new_object_for_class (JAVA_LANG_CLASSCASTEXCEPTION);
  arithmeticException = new_object_for_class (JAVA_LANG_ARITHMETICEXCEPTION);
  arrayIndexOutOfBoundsException = new_object_for_class (JAVA_LANG_ARRAYINDEXOUTOFBOUNDSEXCEPTION);
//  threadDeath = new_object_for_class (JAVA_LANG_THREADDEATH);
}

/**
 * @return false iff all threads are dead.
 */
void throw_exception (Object *exception)
{
  Thread *auxThread;
  
  #ifdef VERIFY
  assert (exception != null, EXCEPTIONS0);
  #endif VERIFY

  if (currentThread == null)
  {
    // No threads have started probably
    return;
  }
  
  #ifdef VERIFY
  assert (currentThread->state != DEAD, EXCEPTIONS1);
  #endif VERIFY
  
  gExceptionPc = pc;
  gExcepMethodRec = null;

  #if 0
  trace (-1, get_class_index(exception), 3);
  #endif

 LABEL_PROPAGATE:
  tempStackFrame = current_stackframe();
  tempMethodRecord = tempStackFrame->methodRecord;

  if (gExcepMethodRec == null)
    gExcepMethodRec = tempMethodRecord;
  gExceptionRecord = (ExceptionRecord *) (get_binary_base() + tempMethodRecord->exceptionTable);
  tempCurrentOffset = ptr2word(pc) - ptr2word(get_binary_base() + tempMethodRecord->codeOffset);

  #if 0
  trace (-1, tempCurrentOffset, 5);
  #endif

  gNumExceptionHandlers = tempMethodRecord->numExceptionHandlers;
  while (gNumExceptionHandlers--)
  {
    if (tempCurrentOffset >= gExceptionRecord->start && tempCurrentOffset <= gExceptionRecord->end)
    {
      // Check if exception class applies
      if (instance_of (exception, gExceptionRecord->classIndex))
      {
        // Clear operand stack
        init_stack_ptr (tempStackFrame, tempMethodRecord);
        // Push the exception object
        push_ref (ptr2word (exception));
        // Jump to handler:
        pc = get_binary_base() + tempMethodRecord->codeOffset + 
             gExceptionRecord->handler;
        return;
      }
    }
    gExceptionRecord++;
  }
  // No good handlers in current stack frame - go up.
  auxThread = currentThread;
  do_return (0);
  // Note: return takes care of synchronized methods.
  if (auxThread->state == DEAD)
  {
    if (get_class_index(exception) != JAVA_LANG_THREADDEATH)
    {
      handle_uncaught_exception (exception, auxThread,
  			         gExcepMethodRec, tempMethodRecord,
			         gExceptionPc);
    }
    return;
  }
  goto LABEL_PROPAGATE; 
}




