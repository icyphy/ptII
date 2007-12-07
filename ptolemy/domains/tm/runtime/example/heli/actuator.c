#include "actuator.h"
#include "actuators.h"
#include "socket.h"
#include "return.h"
#include "ports.h"
#include <sys/time.h>
#include <stdio.h>
#include <unistd.h>

extern PORT_ACTUATOR_t global_var_ACTUATOR;

#define PLANT_IP "127.0.0.1"
#define PLANT_ACTUATORS_PORT 49154

int ACTUATOR_socket;
struct sockaddr_in ACTUATOR_address;

void ACTUATOR_init() {
  if (OK != udp_client_init(&ACTUATOR_socket,
      &ACTUATOR_address,
      PLANT_IP,
      PLANT_ACTUATORS_PORT)) {
    printf("In ACTUATOR_init: error ");
    printf("in call to udp_client_init.\n");
  } else {
    printf("In ACTUATOR_init.");
  }
}

void ACTUATOR_start() {
  printf("In ACTUATOR_start()\n");
}

char ACTUATOR_isReady() {
  return 1;
}

void ACTUATOR_exec() {

}

void ACTUATOR_stopExec() {
  // Set the stop flag.
  printf("In ACTUATOR_stopExec. Do nothing\n");
}

void ACTUATOR_produceOutput() {
  actuators_mesg_t actuators_mesg;

  actuators_mesg.y19 = global_var_ACTUATOR.v1;
  actuators_mesg.y20 = global_var_ACTUATOR.v2;
  actuators_mesg.y21 = global_var_ACTUATOR.v3;
  actuators_mesg.y22 = global_var_ACTUATOR.v4;

  if (OK != udp_send(ACTUATOR_socket,
      &ACTUATOR_address,
      &global_var_ACTUATOR,
      sizeof(global_var_ACTUATOR))) {
    printf("In ACTUATOR_produceOutput: ");
    printf("error in call to udp_send\n");
  }
  printf("In ACTUATOR_produceOutput: ");
  printf("%.3f %.3f %.3f %.3f \n",
      actuators_mesg.y19,
      actuators_mesg.y20,
      actuators_mesg.y21,
      actuators_mesg.y22);
}


void ACTUATOR_getDeadline(struct timeval* tm) {
  printf("In ACTUATOR_getDeadline.");
  printf(" The deadline is 0sec.\n");
}
