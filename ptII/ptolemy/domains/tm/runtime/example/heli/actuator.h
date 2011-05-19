#ifndef __ACTUATOR_H__
#define __ACTUATOR_H__

#include <sys/time.h>
#include "ports.h"

// Execution functions for actor1.
void ACTUATOR_init();
void ACTUATOR_start();
char ACTUATOR_isReady();
void ACTUATOR_exec();
void ACTUATOR_stopExec();
void ACTUATOR_produceOutput();
void ACTUATOR_getDeadline(struct timeval* tm);

#endif
