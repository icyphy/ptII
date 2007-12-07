#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include "isr.h"
#include "ports.h"

void ISR_init() {
  // do nothing for now.
}

void ISR_start() {
  // start a thread that create a GPS call every ***period.
  pthread_t gpsThread;
  int rc;

  rc = pthread_create(&gpsThread, NULL, runGPS, NULL);
  if (rc) {
    printf("ERROR; return code from pthread_create() is %d\n", rc);
    exit(-1);
  }
}

void* runGPS() {
  PORT_GPS_t GPS_val;

  while(1) {
    printf("Receive GPS packet.\n");
    GPS_val.v1 = 0.0;
    GPS_val.v2 = 0.0;
    setGPS(&GPS_val);
    usleep(INTERRUPT_PERIOD_uSEC);
  }
}
