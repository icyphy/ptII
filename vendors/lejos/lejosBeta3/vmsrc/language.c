/**
 * Runtime data structures for loaded program.
 */

#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "exceptions.h"
#include "stack.h"
#include "platform_hooks.h"

#ifdef VERIFY
boolean classesInitialized = false;
#endif

#if 0
#define get_stack_object(MREC_)  ((Object *) get_ref_at ((MREC_)->numParameters - 1))
#endif

// Reliable globals:

void *installedBinary;

// Temporary globals:

// (Gotta be careful with these; a lot of stuff
// is not reentrant because of globals like these).

static ClassRecord *tempClassRecord;
static MethodRecord *tempMethodRecord;

// Methods:

byte get_class_index (Object *obj)
{
  byte f;

  f = obj->flags;
  if (f & IS_ARRAY_MASK)
    return JAVA_LANG_OBJECT;
  return (f & CLASS_MASK);
}

/**
 * @return Method record or null.
 */
MethodRecord *find_method (ClassRecord *classRecord, TWOBYTES methodSignature)
{
  tempByte = classRecord->numMethods;
  while (tempByte--)
  {
    tempMethodRecord = get_method_record (classRecord, tempByte);
    if (tempMethodRecord->signatureId == methodSignature)
      return tempMethodRecord;
  }
  return null;
}

boolean dispatch_static_initializer (ClassRecord *aRec, byte *retAddr)
{
  if (is_initialized (aRec))
    return false;
  set_initialized (aRec);
  if (!has_clinit (aRec))
    return false;
  #if DEBUG_METHODS
  printf ("dispatch_static_initializer: has clinit: %d, %d\n",
          (int) aRec, (int) retAddr);
  #endif
  dispatch_special (aRec, find_method (aRec, _CLINIT__V), retAddr);
  return true;
}

void dispatch_virtual (Object *ref, TWOBYTES signature, byte *retAddr)
{
  MethodRecord *auxMethodRecord;
  byte auxByte;

  if (ref == JNULL)
  {
    throw_exception (nullPointerException);
    return;
  }
  auxByte = get_class_index(ref);
 LABEL_METHODLOOKUP:
  tempClassRecord = get_class_record (auxByte);
  auxMethodRecord = find_method (tempClassRecord, signature);
  if (auxMethodRecord == null)
  {
    #if SAFE
    if (auxByte == JAVA_LANG_OBJECT)
    {
      throw_exception (noSuchMethodError);
      return;
    }
    #endif
    auxByte = tempClassRecord->parentClass;
    goto LABEL_METHODLOOKUP;
  }
  if (dispatch_special (tempClassRecord, auxMethodRecord, retAddr))
  {
    if (is_synchronized(auxMethodRecord))
    {
      current_stackframe()->monitor = ref;
      enter_monitor (ref);
    }
  }
}

/**
 * Calls static initializer if necessary before
 * dispatching with dispatch_special().
 * @param retAddr Return bytecode address.
 * @param btAddr Backtrack bytecode address (in case
 *               static initializer is executed).
 */
void dispatch_special_checked (byte classIndex, byte methodIndex,
                               byte *retAddr, byte *btAddr)
{
  ClassRecord *classRecord;

  #if DEBUG_METHODS
  printf ("dispatch_special_checked: %d, %d, %d, %d\n",
          classIndex, methodIndex, (int) retAddr, (int) btAddr);
  #endif

  classRecord = get_class_record (classIndex);
  if (dispatch_static_initializer (classRecord, btAddr))
    return;
  dispatch_special (classRecord, get_method_record (classRecord, methodIndex),
                    retAddr);
}

/**
 * @param classRecord Record for method class.
 * @param methodRecord Calle's method record.
 * @param retAddr What the PC should be upon return.
 * @return true iff the stack frame was pushed.
 */
boolean dispatch_special (ClassRecord *classRecord, MethodRecord *methodRecord, 
                          byte *retAddr)
{
  #if DEBUG_METHODS
  int debug_ctr;
  #endif

  StackFrame *stackFrame;
  byte newStackFrameIndex;

  #if DEBUG_BYTECODE
  printf ("\n------ dispatch special - %d ------------------\n\n",
          methodRecord->signatureId);
  #endif

  #if DEBUG_METHODS
  printf ("dispatch_special: %d, %d, %d\n", 
          (int) classRecord, (int) methodRecord, (int) retAddr);
  printf ("-- signature id = %d\n", methodRecord->signatureId);
  printf ("-- code offset  = %d\n", methodRecord->codeOffset);
  printf ("-- flags        = %d\n", methodRecord->mflags);
  printf ("-- num params   = %d\n", methodRecord->numParameters);
  printf ("-- stack ptr    = %d\n", (int) get_stack_ptr());
  #endif

  pop_words (methodRecord->numParameters);
  pc = retAddr;

  if (is_native (methodRecord))
  {
    dispatch_native (methodRecord->signatureId, get_stack_ptr() + 1);
    // Stack frame not pushed
    return false;
  }

  newStackFrameIndex = currentThread->stackFrameArraySize;
  if (newStackFrameIndex >= MAX_STACK_FRAMES)
  {
    throw_exception (stackOverflowError);
    return false;
  }
  if (newStackFrameIndex == 0)
  {
    // Assign NEW stack frame
    stackFrame = stackframe_array();
  }
  else
  {
    #if DEBUG_METHODS
    for (debug_ctr = 0; debug_ctr < methodRecord->numParameters; debug_ctr++)
      printf ("-- param[%d]    = %ld\n", debug_ctr, (long) get_stack_ptr()[debug_ctr+1]);  
    #endif

    // Save OLD stackFrame state
    stackFrame = stackframe_array() + (newStackFrameIndex - 1);
    update_stack_frame (stackFrame);
    // Push NEW stack frame
    stackFrame++;
  }
  // Increment size of stack frame array
  currentThread->stackFrameArraySize++;
  // Initialize rest of new stack frame
  stackFrame->methodRecord = methodRecord;
  stackFrame->monitor = null;
  stackFrame->localsBase = get_stack_ptr() + 1;
  stackFrame->isReferenceBase = get_is_ref_ptr() + 1;
  // Initialize auxiliary global variables (registers)
  pc = get_code_ptr(methodRecord);

  #ifdef DEBUG_METHODS
  printf ("pc set to 0x%X\n", (int) pc);
  #endif

  init_stack_ptr (stackFrame, methodRecord);
  update_constant_registers (stackFrame);
  
  //printf ("m %d stack = %d\n", (int) methodRecord->signatureId, (int) (localsBase - stack_array())); 
  
  // Check for stack overflow
  if (is_stack_overflow (methodRecord))
  {
    throw_exception (stackOverflowError);
    return false;
  } 
  return true;
}

/**
 */
void do_return (byte numWords)
{
  StackFrame *stackFrame;
  STACKWORD *fromStackPtr;
  boolean *fromIsRefPtr;

  stackFrame = current_stackframe();

  #if DEBUG_BYTECODE
  printf ("\n------ return ----- %d ------------------\n\n",
          stackFrame->methodRecord->signatureId);
  #endif

  #ifdef DEBUG_METHODS
  printf ("do_return: method: %d  #  num. words: %d\n", 
          stackFrame->methodRecord->signatureId, numWords);
  #endif

  #ifdef VERIFY
  assert (stackFrame != null, LANGUAGE3);
  #endif
  if (stackFrame->monitor != null)
    exit_monitor (stackFrame->monitor);

  #if DEBUG_THREADS
  printf ("do_return: stack frame array size: %d\n", currentThread->stackFrameArraySize);
  #endif

  if (currentThread->stackFrameArraySize == 1)
  {
    #if DEBUG_METHODS
    printf ("do_return: thread is done: %d\n", (int) currentThread);
    #endif
    currentThread->state = DEAD;
    schedule_request (REQUEST_SWITCH_THREAD);
    return;
  }

  // Place source ptr below data to be copied up the stack
  fromStackPtr = get_stack_ptr_at (numWords);
  fromIsRefPtr = get_is_ref_ptr_at (numWords);
  // Pop stack frame
  currentThread->stackFrameArraySize--;
  stackFrame--;
  // Assign registers
  update_registers (stackFrame);

  #if DEBUG_METHODS
  printf ("do_return: stack reset to:\n");
  printf ("-- stack ptr = %d\n", (int) get_stack_ptr());
  #endif

  while (numWords--)
  {
    push_word_or_ref (*(++fromStackPtr), *(++fromIsRefPtr));
  }  
}

/**
 * @return 1 or 0.
 */
STACKWORD instance_of (Object *obj, byte classIndex)
{
  byte rtType;

  if (obj == null)
    return 0;
  rtType = get_class_index(obj);
  // TBD: support for interfaces
  if (is_interface (get_class_record(classIndex)))
    return 1;
 LABEL_INSTANCE:
  if (rtType == classIndex)
    return 1;
  if (rtType == JAVA_LANG_OBJECT)
    return 0;
  rtType = get_class_record(rtType)->parentClass;
  goto LABEL_INSTANCE;
}





