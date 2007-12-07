#include "ins_isr.h"
#include "ins.h"
#include "socket.h"
#include "return.h"
#include "ports.h"

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/poll.h>

void INS_ISR_init() {
  // do nothing.
}

void INS_ISR_start() {
  // start a thread that admits INS UDP packates.
  pthread_t insThread;
  int rc;

  rc = pthread_create(&insThread, NULL, runINS, NULL);
  if (rc) {
    printf("ERROR; return code from pthread_create() is %d\n", rc);
    exit(-1);
  }
}


/** Create a UDP socket server and listen to request.
 *  For every request, create a INS event that triggers the filer task.
 */
void* runINS() {
  int ins_socket;
  struct pollfd await;
  ins_mesg_t ins_msg;
  PORT_INS_t INS_val;

#define CONTROLLER_INS_PORT 49152

  if (OK != udp_server_init(&ins_socket, CONTROLLER_INS_PORT)) {
    printf("In INS_ISR: error in call to ");
    printf("udp_server_init for ins\n");
    printf("INS_ISR exits.\n");
    pthread_exit(NULL);
  }

  printf("In GPS_GPS: GPS_GPS started.\n");


  await.fd = ins_socket;
  await.events = POLLIN | POLLPRI;

  while(1) {
    if (poll(&await, 1, -1) == -1) {
      printf("In INS_ISR: error in call to ");
      printf("poll\n");
    }

    if (await.revents & (POLLIN | POLLPRI)) {
      printf("ins receives");

      if (OK != udp_receive(ins_socket,
          &ins_msg,
          sizeof(ins_mesg_t))) {
        printf("In INS_ISR: ");
        printf("error in call to ");
        printf("udp_receive for ins\n");
      }
      INS_val.v1 = ins_msg.y6;
      INS_val.v2 = ins_msg.y7;
      INS_val.v3 = ins_msg.y8;
      INS_val.v4 = ins_msg.y9;
      INS_val.v5 = ins_msg.y10;
      INS_val.v6 = ins_msg.y11;
      setINS(&INS_val);
    }

  }
}







