
#include "types.h"
#include "constants.h"

#ifndef _INTERPRETER_H
#define _INTERPRETER_H

#define REQUEST_EXIT          0
#define REQUEST_SWITCH_THREAD 1

extern boolean gMakeRequest;
extern byte    gRequestCode;

extern byte *pc;
extern STACKWORD *stackTop;
extern STACKWORD *localsBase;
extern boolean *isReference;
extern boolean *isReferenceBase;

// Temp globals:

extern byte tempByte;
extern STACKWORD tempStackWord;

extern void engine();

static inline void schedule_request (const byte aCode)
{
  gMakeRequest = true;
  gRequestCode = aCode;
}

#endif _INTERPRET_H





