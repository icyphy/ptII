#ifndef __PORTS_H__
#define __PORTS_H__

#include <pthread.h>
#include <sys/types.h>

typedef struct PORT_GPS {
  float v1;
  float v2;
  pthread_mutex_t GPS_Mutex;
  char isNew;
} PORT_GPS_t;

typedef struct PORT_INS {
  float v1;
  float v2;
  float v3;
  float v4;
  float v5;
  float v6;
  pthread_mutex_t INS_Mutex;
  char isNew;
} PORT_INS_t;

typedef struct PORT_STATEFB {
  float v1;
  float v2;
  float v3;
  float v4;
  float v5;
  float v6;
  pthread_mutex_t STATEFB_Mutex;
  char isNew;
} PORT_STATEFB_t;

typedef struct PORT_ACTUATOR {
  float v1;
  float v2;
  float v3;
  float v4;
  pthread_mutex_t ACTUATOR_Mutex;
  char isNew;
} PORT_ACTUATOR_t;

void ports_init();
void setGPS(PORT_GPS_t* GPS_val);
void setINS(PORT_INS_t* INS_val);
void setSTATEFB(PORT_STATEFB_t* STATEFB_val);
void setACTUATOR(PORT_ACTUATOR_t* ACTUATOR_val);

#endif
