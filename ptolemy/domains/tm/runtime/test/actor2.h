#ifndef __ACTOR2_H__
#define __ACTOR2_H__

#include <sys/time.h>
#include "ports.h"

// Execution functions for actor1.
void ACTOR2_init();
void ACTOR2_start();
char ACTOR2_isReady();
void ACTOR2_exec();
void ACTOR2_stopExec();
void ACTOR2_produceOutput();
void ACTOR2_getDeadline(struct timeval* tm);

#endif
