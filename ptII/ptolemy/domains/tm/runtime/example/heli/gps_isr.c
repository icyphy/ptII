#include "gps_isr.h"
#include "gps.h"
#include "socket.h"
#include "return.h"
#include "ports.h"
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/poll.h>

void GPS_ISR_init() {
  // do nothing.
}

void GPS_ISR_start() {
  // start a thread that admits GPS UDP packates.
  pthread_t gpsThread;
  int rc;

  rc = pthread_create(&gpsThread, NULL, runGPS, NULL);
  if (rc) {
    printf("ERROR; return code from pthread_create() is %d\n", rc);
    exit(-1);
  }
}


/** Create a UDP socket server and listen to request.
 *  For every request, create a GPS event that triggers the filer task.
 */
void* runGPS() {
  int gps_socket;
  struct pollfd await;
  gps_mesg_t gps_msg;
  PORT_GPS_t GPS_val;

#define CONTROLLER_GPS_PORT 49153

  if (OK != udp_server_init(&gps_socket, CONTROLLER_GPS_PORT)) {
    printf("In GPS_ISR: error in call to ");
    printf("udp_server_init for gps\n");
    printf("GPS_ISR exits.\n");
    pthread_exit(NULL);
  }

  printf("In GPS_ISR: GPS_ISR started.\n");

  await.fd = gps_socket;
  await.events = POLLIN | POLLPRI;

  while(1) {
    if (poll(&await, 1, -1) == -1) {
      printf("In GPS_ISR: error in call to ");
      printf("poll\n");
    }

    if (await.revents & (POLLIN | POLLPRI)) {
      printf("gps receives");

      if (OK != udp_receive(gps_socket,
          &gps_msg,
          sizeof(gps_mesg_t))) {
        printf("In GPS_ISR: ");
        printf("error in call to ");
        printf("udp_receive for gps\n");
      }
      GPS_val.v1 = gps_msg.this;
      GPS_val.v2 = gps_msg.that;
      setGPS(&GPS_val);
    }

  }
}







