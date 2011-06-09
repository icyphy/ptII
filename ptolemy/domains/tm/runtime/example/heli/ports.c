#include <stdio.h>
#include "ports.h"
#include "tmrt.h"
#include "schedule.h"
#include "filter.h"
#include "statefb.h"
#include "actuator.h"

// Ports as global variables.
PORT_GPS_t global_var_GPS;
PORT_INS_t global_var_INS;
PORT_STATEFB_t global_var_STATEFB;
PORT_ACTUATOR_t global_var_ACTUATOR;

/** Initialize ports, in particular, the mutex variables that guards
 *  the reads and writes of them.
 */
void ports_init() {
  pthread_mutex_init(&(global_var_GPS.GPS_Mutex), NULL);
  pthread_mutex_init(&(global_var_INS.INS_Mutex), NULL);
  pthread_mutex_init(&(global_var_STATEFB.STATEFB_Mutex), NULL);
  pthread_mutex_init(&(global_var_ACTUATOR.ACTUATOR_Mutex), NULL);
}

void setGPS(PORT_GPS_t* GPS_val) {
  TM_TASK_t* newTask;

  pthread_mutex_lock(&(global_var_GPS.GPS_Mutex));
  global_var_GPS.v1 = GPS_val->v1;
  global_var_GPS.v2 = GPS_val->v2;
  global_var_GPS.isNew = 1;
  pthread_mutex_unlock(&(global_var_GPS.GPS_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = FILTER_isReady;
  newTask->exec = FILTER_exec;
  newTask->stopExec = FILTER_stopExec;
  newTask->produceOutput = FILTER_produceOutput;
  newTask->getDeadline = FILTER_getDeadline;
  newTask->hasDeadline = 1;

  insertTriggeredTask(newTask);
  printf("new task FILTER inserted to the scheduler.\n");
}

void setINS(PORT_INS_t* INS_val) {
  TM_TASK_t* newTask;
  pthread_mutex_lock(&(global_var_INS.INS_Mutex));
  global_var_INS.v1 = INS_val->v1;
  global_var_INS.v2 = INS_val->v2;
  global_var_INS.v3 = INS_val->v3;
  global_var_INS.v4 = INS_val->v4;
  global_var_INS.v5 = INS_val->v5;
  global_var_INS.v6 = INS_val->v6;
  global_var_INS.isNew = 1;
  pthread_mutex_unlock(&(global_var_INS.INS_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = FILTER_isReady;
  newTask->exec = FILTER_exec;
  newTask->stopExec = FILTER_stopExec;
  newTask->produceOutput = FILTER_produceOutput;
  newTask->getDeadline = FILTER_getDeadline;
  newTask->hasDeadline = 1;

  insertTriggeredTask(newTask);
}

void setSTATEFB(PORT_STATEFB_t* STATEFB_val){
  TM_TASK_t* newTask;
  pthread_mutex_lock(&(global_var_STATEFB.STATEFB_Mutex));
  global_var_STATEFB.v1 = STATEFB_val->v1;
  global_var_STATEFB.v2 = STATEFB_val->v2;
  global_var_STATEFB.v3 = STATEFB_val->v3;
  global_var_STATEFB.v4 = STATEFB_val->v4;
  global_var_STATEFB.v5 = STATEFB_val->v5;
  global_var_STATEFB.v6 = STATEFB_val->v6;
  global_var_STATEFB.isNew = 1;
  pthread_mutex_unlock(&(global_var_STATEFB.STATEFB_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = STATEFB_isReady;
  newTask->exec = STATEFB_exec;
  newTask->stopExec = STATEFB_stopExec;
  newTask->produceOutput = STATEFB_produceOutput;
  newTask->getDeadline = STATEFB_getDeadline;
  newTask->hasDeadline = 1;
  insertTriggeredTask(newTask);
}

void setACTUATOR(PORT_ACTUATOR_t* ACTUATOR_val) {
  TM_TASK_t* newTask;
  pthread_mutex_lock(&(global_var_ACTUATOR.ACTUATOR_Mutex));
  global_var_ACTUATOR.v1 = ACTUATOR_val->v1;
  global_var_ACTUATOR.v2 = ACTUATOR_val->v2;
  global_var_ACTUATOR.v3 = ACTUATOR_val->v3;
  global_var_ACTUATOR.v4 = ACTUATOR_val->v4;
  global_var_ACTUATOR.isNew = 1;
  pthread_mutex_unlock(&(global_var_ACTUATOR.ACTUATOR_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = ACTUATOR_isReady;
  newTask->exec = ACTUATOR_exec;
  newTask->stopExec = ACTUATOR_stopExec;
  newTask->produceOutput = ACTUATOR_produceOutput;
  newTask->getDeadline = ACTUATOR_getDeadline;
  newTask->hasDeadline = 0;
  insertTriggeredTask(newTask);
}
