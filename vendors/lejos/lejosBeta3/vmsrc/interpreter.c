
#include "interpreter.h"
#include "threads.h"
#include "trace.h"
#include "types.h"
#include "constants.h"
#include "classes.h"
#include "opcodes.h"
#include "configure.h"
#include "memory.h"
#include "language.h"
#include "exceptions.h"
#include "specialclasses.h"
#include "fields.h"
#include "stack.h"


#define F_OFFSET_MASK  0x0F

#if DEBUG_BYTECODE
extern char *OPCODE_NAME[];
#endif

// Interpreter globals:

boolean gMakeRequest;
byte    gRequestCode;

byte *pc;
STACKWORD *localsBase;
STACKWORD *stackTop;
boolean *isReference;
boolean *isReferenceBase;

// Temporary globals:

byte tempByte;
byte *tempBytePtr;
JFLOAT tempFloat;
ConstantRecord *tempConstRec;
STACKWORD tempStackWord;
STACKWORD *tempWordPtr;
JINT tempInt;
  
/**
 * Assumes pc points to 2-byte offset, and jumps.
 */
void do_goto (boolean aCond)
{
  if (aCond)
  {
    pc += (JSHORT) (((TWOBYTES) pc[0] << 8) | pc[1]);
    pc--;
  }
  else
  {
    pc += 2;
  }
}

void do_isub (void)
{
  STACKWORD poppedWord;

  poppedWord = pop_word();
  set_top_word (word2jint(get_top_word()) - word2jint(poppedWord));
}

#if FP_ARITHMETIC

void do_fcmp (JFLOAT f1, JFLOAT f2, STACKWORD def)
{
  if (f1 > f2)
    push_word (1);
  else if (f1 == f2)
    push_word (0);
  else if (f1 < f2)
    push_word (-1);
  else 
    push_word (def);
}

#endif

/**
 * @return A String instance, or JNULL if an exception was thrown
 *         or the static initializer of String had to be executed.
 */
static inline Object *create_string (ConstantRecord *constantRecord, 
                                     byte *btAddr)
{
  Object *ref;
  Object *arr;
  JINT    i;

  ref = new_object_checked (JAVA_LANG_STRING, btAddr);
  if (ref == JNULL)
    return JNULL;
  arr = new_primitive_array (T_CHAR, constantRecord->constantSize);
  if (arr == JNULL)
  {
    deallocate (obj2ptr(ref), class_size (JAVA_LANG_STRING));    
    return JNULL;
  }
  
  //printf ("char array at %d\n", (int) arr);
  
  store_word ((byte *) &(((String *) ref)->characters), 4, obj2word(arr));
  
  for (i = 0; i < constantRecord->constantSize; i++)
  {
    jchar_array(arr)[i] = (JCHAR) get_constant_ptr(constantRecord)[i];

    //printf ("char %d: %c\n", i, (char) (jchar_array(arr)[i])); 
  }
  return ref;
}

/**
 * Pops the array index off the stack, assigns
 * both tempInt and tempBytePtr, and checks
 * bounds and null reference. The array reference
 * is the top word on the stack after this operation.
 * @return True if successful, false if an exception has been scheduled.
 */
boolean array_load_helper()
{
  tempInt = word2jint(pop_word());
  tempBytePtr = word2ptr(get_top_ref());
  if (tempBytePtr == JNULL)
    throw_exception (nullPointerException);
  else if (tempInt < 0 || tempInt >= get_array_length ((Object *) tempBytePtr))
    throw_exception (arrayIndexOutOfBoundsException);
  else
    return true;
  return false;
}

/**
 * Same as array_load_helper, except that it pops
 * the reference from the stack.
 */
boolean array_store_helper()
{
  if (array_load_helper())
  {
    pop_ref();
    return true;
  }
  return false;    
}

/**
 * Everything runs inside here, essentially.
 * Notes:
 * 1. currentThread must be initialized.
 */
void engine()
{
  register short numOpcodes;
  
  gMakeRequest = false;
  switch_thread();
  numOpcodes = OPCODES_PER_TIME_SLICE;
 LABEL_ENGINELOOP: 
  if (!(--numOpcodes))
  {
    #if DEBUG_THREADS
    printf ("switching thread: %d\n", (int) numOpcodes);
    #endif
    schedule_request (REQUEST_SWITCH_THREAD);
    #if DEBUB_THREADS
    printf ("done switching thread\n");
    #endif
    numOpcodes = OPCODES_PER_TIME_SLICE;
  }
  if (gMakeRequest)
  {
    gMakeRequest = false;
    switch (gRequestCode)
    {
      case REQUEST_SWITCH_THREAD:
        if (!switch_thread())
          return;
	break;
      case REQUEST_EXIT:
        return;
    }
  }

  //-----------------------------------------------
  // SWITCH BEGINS HERE
  //-----------------------------------------------

  #if DEBUG_BYTECODE
  printf ("0x%X: \n", (int) pc);
  printf ("OPCODE (0x%X) %s\n", (int) *pc, OPCODE_NAME[*pc]);
  #endif

  switch (*pc++)
  {

    #include "op_stack.hc"
    #include "op_locals.hc"
    #include "op_arrays.hc"
    #include "op_objects.hc"
    #include "op_control.hc"
    #include "op_methods.hc"
    #include "op_other.hc"
    #include "op_skip.hc"
    #include "op_conversions.hc"
    #include "op_logical.hc"
    #include "op_arithmetic.hc"
    
  }

  //-----------------------------------------------
  // SWITCH ENDS HERE
  //-----------------------------------------------

  // This point should never be reached

  #ifdef VERIFY
  assert (false, 1000 + *pc);
  #endif VERIFY
}

