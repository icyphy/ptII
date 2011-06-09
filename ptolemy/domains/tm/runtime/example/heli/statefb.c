#include "statefb.h"
#include "ports.h"
#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>
#include <math.h>

#define STATEFB_PERIOD 5000

extern PORT_STATEFB_t global_var_STATEFB;

struct timeval STATEFB_startingTime;
//char STATEFB_triggerCount;
//int STATEFB_execCount;
long STATEFB_lastDeadlineSec;
long STATEFB_lastDeadlineuSec;
PORT_ACTUATOR_t STATEFB_local_output;

void STATEFB_init() {
  printf("In STATEFB_init()\n");
  //  STATEFB_triggerCount = 0;
  //  STATEFB_execCount = 0;
  gettimeofday(&STATEFB_startingTime, NULL);
  STATEFB_lastDeadlineSec = STATEFB_startingTime.tv_sec;
  STATEFB_lastDeadlineuSec = STATEFB_startingTime.tv_usec;
}

void STATEFB_start() {
  printf("In STATEFB_start()\n");
}

char STATEFB_isReady() {
  static char STATEFB_triggerCount = 0;

  // fillin here.
  STATEFB_triggerCount++;
  printf("STATEFB_isReady: triggerCount = %d (2 is 0)\n", STATEFB_triggerCount);
  if (STATEFB_triggerCount == 1) {
    return 1;
  } else {
    STATEFB_triggerCount = 0;
    return 0;
  }
}

void STATEFB_exec() {
  // Enter the execution code here.

  float y6 = global_var_STATEFB.v1;
  float y7 = global_var_STATEFB.v2;
  float y8 = global_var_STATEFB.v3;
  float y9 = global_var_STATEFB.v4;
  float y10 = global_var_STATEFB.v5;
  float y11 = global_var_STATEFB.v6;

  float desired_u = 0.0;
  float desired_v = 0.0;
  float desired_w = 0.0;
  float desired_phi = 0.0;
  static float iteration = 0;


  float y22;

  printf("In STATEFB_exec iteration: #%f.0. \n", iteration);

#define UP_ITERATIONS (300.0)
#define GO_ITERATIONS (100.0)
#define TURN_ITERATIONS (50.0)
#define PI (3.14159265358)
#define DOWN_BIAS (1.1) /* 1.125 */

        iteration++;

        if (iteration < UP_ITERATIONS)
        { /* go up */
                desired_u =    0;
                desired_w = -0.5;
                desired_phi =    0;
        }
          else if (iteration < UP_ITERATIONS + GO_ITERATIONS)
        { /* go north */
                desired_u = 1;
                desired_phi = 0;
        }
        else if (iteration < UP_ITERATIONS + GO_ITERATIONS + TURN_ITERATIONS)
        { /* turn west */
                desired_u = 0;
                desired_w = DOWN_BIAS;
                /* phi starts at 0, ends at -0.5 * PI */
                desired_phi = ((iteration-(UP_ITERATIONS + GO_ITERATIONS))/TURN_ITERATIONS) * -0.5 * PI;
        }
        else if (iteration < UP_ITERATIONS + 2*GO_ITERATIONS + TURN_ITERATIONS)
        { /* go west */
                desired_u = 1;
                desired_phi = -0.5 * PI;
        }
        else if (iteration < UP_ITERATIONS + 2*GO_ITERATIONS + 2*TURN_ITERATIONS)
        { /* turn south */
                desired_u = 0;
                desired_w = DOWN_BIAS;
                /* phi starts at -0.5, ends at -1 * PI */
                desired_phi = ((iteration-(UP_ITERATIONS + 2*GO_ITERATIONS + TURN_ITERATIONS))/TURN_ITERATIONS) * -0.5 * PI - 0.5 * PI;
        }
        else if (iteration < UP_ITERATIONS + 3*GO_ITERATIONS + 2*TURN_ITERATIONS)
        { /* go south */
                desired_u = 1;
                desired_phi = -1 * PI;
        }
        else if (iteration < UP_ITERATIONS + 3*GO_ITERATIONS + 3*TURN_ITERATIONS)
        { /* turn east */
                desired_u = 0;
                desired_w = DOWN_BIAS;
                /* phi starts at -1.0, ends at -1.5 * PI */
                desired_phi = ((iteration-(UP_ITERATIONS + 3*GO_ITERATIONS + 2*TURN_ITERATIONS))/TURN_ITERATIONS) * -0.5 * PI - 1.0 * PI;
        }
        else if (iteration < UP_ITERATIONS + 4*GO_ITERATIONS + 3*TURN_ITERATIONS)
        { /* go east */
                desired_u = 1;
                desired_phi = -1.5 * PI;
        }
        else if (iteration < UP_ITERATIONS + 4*GO_ITERATIONS + 4*TURN_ITERATIONS)
        { /* turn north */
                desired_u = 0;
                desired_w = DOWN_BIAS;
                /* phi starts at -1.5, ends at -2.0 * PI */
                desired_phi = ((iteration-(UP_ITERATIONS + 4*GO_ITERATIONS + 3*TURN_ITERATIONS))/TURN_ITERATIONS) * -0.5 * PI - 1.5 * PI;
        }
        else
        {
                iteration = UP_ITERATIONS;
        }

        desired_v = 0;

        y22 = fmod(desired_phi - y6 + PI, 2 * PI) - PI;

        STATEFB_local_output.v1 =
          (-0.02  * desired_v  ) + ( 0.02  * y8) + ( 0.55 * y10);
        STATEFB_local_output.v2 =
          (-0.02  * desired_u  ) + ( 0.02  * y7) + (-0.55 * y11);
        STATEFB_local_output.v3 =
          ( 0.035 * desired_w  ) + (-0.035 * y9);
        STATEFB_local_output.v4 = y22;
        /* outputs->y22 = ( 1.0   * desired_phi) + (-1.0   * y6); */

        fprintf(stderr, "+");
}

void STATEFB_stopExec() {
  // Set the stop flag.
  printf("In STATEFB_stopExec. Do nothing\n");
}

void STATEFB_produceOutput() {
  // produce output.
  PORT_ACTUATOR_t output;
  output.v1 = STATEFB_local_output.v1;
  output.v2 = STATEFB_local_output.v2;
  output.v3 = STATEFB_local_output.v3;
  output.v4 = STATEFB_local_output.v4;
  setACTUATOR(&output);
}


void STATEFB_getDeadline(struct timeval* tm) {
  printf("In STATEFB_getDeadline.");
  printf("The deadline is 0.002sec.\n");

  STATEFB_lastDeadlineuSec += STATEFB_PERIOD;
  if (STATEFB_lastDeadlineuSec > 1e6) {
    STATEFB_lastDeadlineuSec -= 1e6;
    STATEFB_lastDeadlineSec++;
  }
  tm->tv_sec = STATEFB_lastDeadlineSec;
  tm->tv_usec = STATEFB_lastDeadlineuSec;
}
