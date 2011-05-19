#ifndef __ACTOR1_H__
#define __ACTOR1_H__

#include <sys/time.h>
#include "ports.h"

// Execution functions for actor1.
void ACTOR1_init();
void ACTOR1_start();
char ACTOR1_isReady();
void ACTOR1_exec();
void ACTOR1_stopExec();
void ACTOR1_produceOutput();
void ACTOR1_getDeadline(struct timeval* tm);

#endif
