
#include "types.h"
#include "classes.h"

#ifndef _EXCEPTIONS_H
#define _EXCEPTIONS_H

extern Object *outOfMemoryError;
extern Object *noSuchMethodError;
extern Object *stackOverflowError;
extern Object *nullPointerException;
extern Object *classCastException;
extern Object *arithmeticException;
extern Object *arrayIndexOutOfBoundsException;
//extern Object *threadDeath;

extern void init_exceptions();
extern void throw_exception (Object *throwable);
extern void throw_exception_checked (Object *throwable);

#endif


