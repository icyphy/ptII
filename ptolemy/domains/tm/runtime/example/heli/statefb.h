#ifndef __STATEFB_H__
#define __STATEFB_H__

#include <sys/time.h>
#include "ports.h"

// Execution functions for actor1.
void STATEFB_init();
void STATEFB_start();
char STATEFB_isReady();
void STATEFB_exec();
void STATEFB_stopExec();
void STATEFB_produceOutput();
void STATEFB_getDeadline(struct timeval* tm);

#endif
