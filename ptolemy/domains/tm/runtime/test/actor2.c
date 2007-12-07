#include "actor2.h"
#include <unistd.h>
#include <stdio.h>

#define ACTOR2_PERIOD 2000

struct timeval ACTOR2_startingTime;
int ACTOR2_triggerCount;
int ACTOR2_execCount;
long ACTOR2_lastDeadlineSec;
long ACTOR2_lastDeadlineuSec;

void ACTOR2_init() {
  printf("In ACTOR2_init().\n");
  ACTOR2_triggerCount = 0;
  ACTOR2_execCount = 0;
  gettimeofday(&ACTOR2_startingTime, NULL);
  ACTOR2_lastDeadlineSec = ACTOR2_startingTime.tv_sec;
  ACTOR2_lastDeadlineuSec = ACTOR2_startingTime.tv_usec;
}

void ACTOR2_start() {
  printf("In ACTOR2_start().\n");
}

char ACTOR2_isReady() {
  // fillin here.
  ACTOR2_triggerCount++;
  printf("In ACTOR2_isReady #%d.\n", ACTOR2_triggerCount);
  return 1;
}

void ACTOR2_exec() {
  // Enter the execution code here.
  ACTOR2_execCount++;
  printf("In ACTOR2_exec #%d.\n", ACTOR2_execCount);
  printf("ACTOR2 execution time = 0.001sec\n");
  usleep(1000);
}

void ACTOR2_stopExec() {
  // Set the stop flag.
  printf("In ACTOR2_stopExec.\n");
}

void ACTOR2_produceOutput() {
  // produce output.
  // produce output.
  //PORT_CTRL_t output;
  printf("In ACTOR2_produceOutput.");
  //setCTRL(&output);
}


void ACTOR2_getDeadline(struct timeval* tm) {
  printf("In ACTOR2_getDeadline.");
  printf("The deadline is 0.002sec.\n");

  ACTOR2_lastDeadlineuSec += ACTOR2_PERIOD;
  if (ACTOR2_lastDeadlineuSec > 1e6) {
    ACTOR2_lastDeadlineuSec -= 1e6;
    ACTOR2_lastDeadlineSec++;
  }
  tm->tv_sec = ACTOR2_lastDeadlineSec;
  tm->tv_usec = ACTOR2_lastDeadlineuSec;
}
