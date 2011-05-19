#include "filter.h"
#include "ports.h"
#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>

#define FILTER_PERIOD 1500

extern PORT_GPS_t global_var_GPS;
extern PORT_INS_t global_var_INS;

struct timeval FILTER_startingTime;
//int FILTER_triggerCount;
//int FILTER_execCount;
long FILTER_lastDeadlineSec;
long FILTER_lastDeadlineuSec;
PORT_GPS_t FILTER_GPS;

void FILTER_init() {
  printf("In FILTER_init()\n");
  //FILTER_triggerCount = 0;
  //FILTER_execCount = 0;
  gettimeofday(&FILTER_startingTime, NULL);
  FILTER_lastDeadlineSec = FILTER_startingTime.tv_sec;
  FILTER_lastDeadlineuSec = FILTER_startingTime.tv_usec;
}

void FILTER_start() {
  printf("In FILTER_start()\n");
}

char FILTER_isReady() {
  // fillin here.
  static int FILTER_triggerCount = 0;

  FILTER_triggerCount++;
  printf("FILTER_isReady: being called %d times.\n", FILTER_triggerCount);
  if (global_var_GPS.isNew) {
    FILTER_GPS.v1 = global_var_GPS.v1;
    FILTER_GPS.v2 = global_var_GPS.v2;
    FILTER_GPS.isNew = 1;
    global_var_GPS.isNew = 0;
    return 0;
  } else if (global_var_INS.isNew) {
    // We don't keep a local copy of INS, since we know that the next
    // INS trigger will come after we finish the current reaction.
    global_var_INS.isNew = 0;
    return 1;
  } else {
    // This should not happen.
    printf("FILTER is triggered, but no new event...\n");
    return 0;
  }
}

void FILTER_exec() {
  // Enter the execution code here.
  static int FILTER_execCount = 0;

  FILTER_execCount++;
  printf("In FILTER_exec #%d. This filter does nothing\n", FILTER_execCount);
  printf("FILTER's execution lasts 0.001sec.\n");
  usleep(1000);
}

void FILTER_stopExec() {
  // Set the stop flag.
  printf("In FILTER_stopExec. Do nothing\n");
}

void FILTER_produceOutput() {
  // produce output.
  PORT_STATEFB_t output;
  output.v1 = global_var_INS.v1;
  output.v2 = global_var_INS.v2;
  output.v3 = global_var_INS.v3;
  output.v4 = global_var_INS.v4;
  output.v5 = global_var_INS.v5;
  output.v6 = global_var_INS.v6;

  setSTATEFB(&output);
}


void FILTER_getDeadline(struct timeval* tm) {
  printf("In FILTER_getDeadline.");
  printf("The deadline is 0.002sec.\n");

  FILTER_lastDeadlineuSec += FILTER_PERIOD;
  if (FILTER_lastDeadlineuSec > 1e6) {
    FILTER_lastDeadlineuSec -= 1e6;
    FILTER_lastDeadlineSec++;
  }
  tm->tv_sec = FILTER_lastDeadlineSec;
  tm->tv_usec = FILTER_lastDeadlineuSec;
}
