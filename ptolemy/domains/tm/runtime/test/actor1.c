#include "actor1.h"
#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>

#define ACTOR1_PERIOD 2000

struct timeval ACTOR1_startingTime;
//int ACTOR1_triggerCount;
//int ACTOR1_execCount;
long ACTOR1_lastDeadlineSec;
long ACTOR1_lastDeadlineuSec;

void ACTOR1_init() {
  printf("In ACTOR1_init()\n");
  //ACTOR1_triggerCount = 0;
  //ACTOR1_execCount = 0;
  gettimeofday(&ACTOR1_startingTime, NULL);
  ACTOR1_lastDeadlineSec = ACTOR1_startingTime.tv_sec;
  ACTOR1_lastDeadlineuSec = ACTOR1_startingTime.tv_usec;
}

void ACTOR1_start() {
  printf("In ACTOR1_start()\n");
}

char ACTOR1_isReady() {
  // fillin here.
  static int ACTOR1_triggerCount = 0;

  ACTOR1_triggerCount++;
  printf("In ACTOR1_isReady #%d.\n", ACTOR1_triggerCount);
  return 1;
}

void ACTOR1_exec() {
  // Enter the execution code here.
  static int ACTOR1_execCount = 0;

  ACTOR1_execCount++;
  printf("In ACTOR1_exec #%d.\n", ACTOR1_execCount);
  printf("ACTOR1 execution time = 0.001sec\n");
  usleep(1000);
}

void ACTOR1_stopExec() {
  // Set the stop flag.
  printf("In ACTOR1_stopExec.\n");
}

void ACTOR1_produceOutput() {
  // produce output.
  PORT_CTRL_t output;
  output.v1 = 0.0;
  output.v2 = 1.0;
  output.v3 = 3.0;
  output.v4 = 4.0;

  setCTRL(&output);
}


void ACTOR1_getDeadline(struct timeval* tm) {
  printf("In ACTOR1_getDeadline.");
  printf("The deadline is 0.002sec.\n");

  ACTOR1_lastDeadlineuSec += ACTOR1_PERIOD;
  if (ACTOR1_lastDeadlineuSec > 1e6) {
    ACTOR1_lastDeadlineuSec -= 1e6;
    ACTOR1_lastDeadlineSec++;
  }
  tm->tv_sec = ACTOR1_lastDeadlineSec;
  tm->tv_usec = ACTOR1_lastDeadlineuSec;
}
