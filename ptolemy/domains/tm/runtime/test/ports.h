#ifndef __PORTS_H__
#define __PORTS_H__

#include <pthread.h>
#include <sys/types.h>

typedef struct PORT_GPS {
  double v1;
  double v2;
  pthread_mutex_t GPS_Mutex;
} PORT_GPS_t;

typedef struct PORT_INS {
  double v1;
  double v2;
  double v3;
  double v4;
  double v5;
  double v6;
  pthread_mutex_t INS_Mutex;
} PORT_INS_t;

typedef struct PORT_CTRL {
  double v1;
  double v2;
  double v3;
  double v4;
  pthread_mutex_t CTRL_Mutex;
} PORT_CTRL_t;

void ports_init();
void setGPS(PORT_GPS_t* GPS_val);
void setINS(PORT_INS_t* INS_val);
void setCTRL(PORT_CTRL_t* CTRL_val);

#endif
