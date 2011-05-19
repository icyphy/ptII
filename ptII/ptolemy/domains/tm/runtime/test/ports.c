#include <stdio.h>
#include "ports.h"
#include "tmrt.h"
#include "schedule.h"
#include "actor1.h"
#include "actor2.h"

// Ports as global variables.
PORT_GPS_t varGPS;
PORT_INS_t varINS;
PORT_CTRL_t varCTRL;

/** Initialize ports, in particular, the mutex variables that guards
 *  the reads and writes of them.
 */
void ports_init() {
  pthread_mutex_init(&(varGPS.GPS_Mutex), NULL);
  pthread_mutex_init(&(varINS.INS_Mutex), NULL);
  pthread_mutex_init(&(varCTRL.CTRL_Mutex), NULL);
}

void setGPS(PORT_GPS_t* GPS_val) {
  TM_TASK_t* newTask;

  pthread_mutex_lock(&(varGPS.GPS_Mutex));
  varGPS.v1 = GPS_val->v1;
  varGPS.v2 = GPS_val->v2;
  pthread_mutex_unlock(&(varGPS.GPS_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = ACTOR1_isReady;
  newTask->exec = ACTOR1_exec;
  newTask->stopExec = ACTOR1_stopExec;
  newTask->produceOutput = ACTOR1_produceOutput;
  newTask->getDeadline = ACTOR1_getDeadline;
  newTask->hasDeadline = 1;

  insertTriggeredTask(newTask);
  printf("new task ACTOR1 inserted to the scheduler.\n");
}

void setINS(PORT_INS_t* INS_val) {
  TM_TASK_t* newTask;
  pthread_mutex_lock(&(varINS.INS_Mutex));
  varINS.v1 = INS_val->v1;
  varINS.v2 = INS_val->v2;
  varINS.v3 = INS_val->v3;
  varINS.v4 = INS_val->v4;
  varINS.v5 = INS_val->v5;
  varINS.v6 = INS_val->v6;
  pthread_mutex_unlock(&(varINS.INS_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = ACTOR1_isReady;
  newTask->exec = ACTOR1_exec;
  newTask->stopExec = ACTOR1_stopExec;
  newTask->produceOutput = ACTOR1_produceOutput;
  newTask->getDeadline = ACTOR1_getDeadline;
  newTask->hasDeadline = 1;

  insertTriggeredTask(newTask);
}

void setCTRL(PORT_CTRL_t* CTRL_val){
  TM_TASK_t* newTask;
  pthread_mutex_lock(&(varCTRL.CTRL_Mutex));
  varCTRL.v1 = CTRL_val->v1;
  varCTRL.v2 = CTRL_val->v2;
  varCTRL.v3 = CTRL_val->v3;
  varCTRL.v4 = CTRL_val->v4;
  pthread_mutex_unlock(&(varCTRL.CTRL_Mutex));
  // create the new task.
  newTask = (TM_TASK_t*) malloc(sizeof(TM_TASK_t));
  newTask->isReady = ACTOR2_isReady;
  newTask->exec = ACTOR2_exec;
  newTask->stopExec = ACTOR2_stopExec;
  newTask->produceOutput = ACTOR2_produceOutput;
  newTask->getDeadline = ACTOR2_getDeadline;
  newTask->hasDeadline = 1;
  insertTriggeredTask(newTask);
}
