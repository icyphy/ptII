#ifndef __FILTER_H__
#define __FILTER_H__

#include <sys/time.h>
#include "ports.h"

// Execution functions for actor1.
void FILTER_init();
void FILTER_start();
char FILTER_isReady();
void FILTER_exec();
void FILTER_stopExec();
void FILTER_produceOutput();
void FILTER_getDeadline(struct timeval* tm);

#endif
