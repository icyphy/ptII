/**
 * nativeemul.c
 * Native method handling for unix_impl (emulation).
 */
#include <stdio.h>
#include "types.h"
#include "trace.h"
#include "constants.h"
#include "specialsignatures.h"
#include "specialclasses.h"
#include "stack.h"
#include "memory.h"
#include "threads.h"
#include "classes.h"
#include "language.h"
#include "configure.h"
#include "interpreter.h"
#include "exceptions.h"
#include "platform_config.h"

static TWOBYTES gSensorValue = 0;

/**
 * NOTE: The technique is not the same as that used in TinyVM.
 */
void dispatch_native (TWOBYTES signature, STACKWORD *paramBase)
{
  ClassRecord *classRecord;

  switch (signature)
  {
    case START_V:
      init_thread ((Thread *) word2ptr(paramBase[0]));
      return;
    case YIELD_V:
      switch_thread();
      return;
    case SLEEP_V:
      sleep_thread (paramBase[1]);
      switch_thread();
      return;
    case CURRENTTIMEMILLIS_J:
      push_word (0);
      push_word (get_sys_time());
      return;
    case CALLROM0_V:
      printf ("& ROM call 0: 0x%lX\n", paramBase[0]);
      return;      
    case CALLROM1_V:
      printf ("& ROM call 1: 0x%lX (%ld)\n", paramBase[0], paramBase[1]);
      return;      
    case CALLROM2_V:
      printf ("& ROM call 2: 0x%lX (%ld, %ld)\n", paramBase[0],
                                                  paramBase[1],
                                                  paramBase[2]
             );
      return;      
    case CALLROM3_V:
      printf ("& ROM call 3: 0x%lX (%ld, %ld, %ld)\n", paramBase[0],
                                                     paramBase[1],
                                                     paramBase[2],
                                                     paramBase[3]
             );
      return;      
    case CALLROM4_V:
      printf ("& ROM call 4: 0x%lX (%ld, %ld, %ld, %ld)\n", paramBase[0],
                                                     paramBase[1],
                                                     paramBase[2],
                                                     paramBase[3],
                                                     paramBase[4]
             );
      return;      
    case READMEMORYBYTE_B:
      printf ("& Attempt to read byte from 0x%lX\n", (paramBase[0] & 0xFFFF));
      push_word (0);
      return;
    case WRITEMEMORYBYTE_V:
      printf ("& Attempt to write byte [%lX] at 0x%lX (no effect)\n", paramBase[1] & 0xFF, paramBase[0] & 0xFFFF);
      return;
    case SETMEMORYBIT_V:
      printf ("& Attempt to set memory bit [%ld] at 0x%lX (no effect)\n", paramBase[1] & 0xFF, paramBase[0] & 0xFFFF);
      return;      
    case GETDATAADDRESS_I:
      push_word (ptr2word (((byte *) word2ptr (paramBase[0])) + HEADER_SIZE));
      return;
    case RESETSERIAL_V:
      printf ("& Call to resetRcx");
      return;
    case READSENSORVALUE_I:
      // Parameters: int romId (0..2), int requestedValue (0..2).
      if (gSensorValue > 100)
	gSensorValue = 0;
      push_word (gSensorValue++);
      return;
    case SETSENSORVALUE_V:
      // Arguments: int romId (1..3), int value, int requestedValue (0..3) 
      gSensorValue = paramBase[1];
      return;
    default:
      throw_exception (noSuchMethodError);
  }
} 


