/** Copyright(c) 2002, Jie Liu
 *  ALl right reserved.
 *
 *  Timed-Multitasking Runtime
 *
 *  $Id$
 */

#ifndef __SCHEDULE_H__
#define __SCHEDULE_H__

#include <pthread.h>
#include <sys/time.h>
#include <stdlib.h>

typedef struct TM_TASK {
  char (*isReady)();
  void (*exec)();
  void (*stopExec)();
  void (*produceOutput)();
  void (*getDeadline)(struct timeval *tv);
  char hasDeadline;
} TM_TASK_t;

#define TASK_TRIGGERED     0
#define TASK_EXECUTING     1
#define TASK_EXEC_DONE     2
#define TASK_TIME_OUT      3
#define TASK_FINISHED      4

typedef struct sched_entry {
  TM_TASK_t* sch_task;
  char sch_status;
} SCHED_ENTRY_t;

#define MAX_SCHEDULE_ENTRY 10

typedef struct task_table {
  SCHED_ENTRY_t* table[MAX_SCHEDULE_ENTRY];
  int insertPoint;
  int removePoint;
  pthread_cond_t newTaskCond;
  pthread_mutex_t taskEntryMutex;
} TASK_TABLE_t;


#endif
