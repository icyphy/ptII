/**
 * gc.c
 * Garbage collection routines
 */
 
#include "trace.h"
#include "classes.h"
#include "language.h"

byte gCurrentMark = 0;



void mark_and_sweep()
{

$TBD
	
}

void mark (Object *obj)
{
  if (obj == JNULL)
    return;

  #ifdef VERIFY_GC
  assert (is_allocated (obj), GC0);
  #endif

  if (is_gc_marked (obj))
    return;
  set_gc_marked (obj);
  if (is_array (obj))
  {
    if (get_element_type (obj) == T_REFERENCE)
    {
      unsigned short i;
      unsigned short length = get_array_length (obj);
      REFERENCE *refarr = ref_array (obj);
      
      for (i = 0; i < length; i++)
	mark (refarr[i]);
    }
  }
  else
  {
    ClassRecord *classRecord;
    byte classIndex;
    
    classIndex = get_na_class_index (obj);
    for (;;)
    {
      classRecord = get_class_record (classIndex);
      // Mark fields of type REFERENCE.
      mark_reference_fields (obj, classRecord);
      if (classIndex == JAVA_LANG_OBJECT)
	break;
      classIndex = classRecord -> parentClass;
    } 
  }
}
