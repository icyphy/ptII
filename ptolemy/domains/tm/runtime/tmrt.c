/** Copyright(c) 2002, Jie Liu
 *  ALl right reserved.
 *
 *  Timed-Multitasking Runtime
 *
 *  $Id$
 */

#include "tmrt.h"
#include "schedule.h"
#include <stdio.h>
#include <unistd.h>

TASK_TABLE_t triggeredTasks;

//TASK_LIST_t executingTasks;

// Need to lock this mutex when set and read the status.
// Although it is more logical to have one of these per task,
// we use this single one to reduce the overhead of initializing
// mutexes.
pthread_mutex_t schStatusMutex;

inline char hasTriggeredTask() {
  return (triggeredTasks.removePoint >= 0);
}

/** This function returns the next task to be executed. If there
 *  is no tasks to be executed, then it blocking reads on the
 *  conditional variable newTaskCond.
 */
void getNextTriggeredTask(SCHED_ENTRY_t** task) {
  pthread_mutex_lock(&(triggeredTasks.taskEntryMutex));
  while (!hasTriggeredTask()) {
    pthread_cond_wait( &(triggeredTasks.newTaskCond),
        &(triggeredTasks.taskEntryMutex));
  }
  *task = triggeredTasks.table[triggeredTasks.removePoint];
  pthread_mutex_unlock(&(triggeredTasks.taskEntryMutex));
}

/** Remove the task pointed by the removePoint from the triggeredTasks
 *  table. If there is no entry in the table, then do nothing.
 */
void removeNextTriggeredTask() {
  pthread_mutex_lock(&(triggeredTasks.taskEntryMutex));
  if (triggeredTasks.removePoint < 0) {
     pthread_mutex_unlock(&(triggeredTasks.taskEntryMutex));
     return;
  }
  triggeredTasks.table[triggeredTasks.removePoint] = NULL;
  triggeredTasks.removePoint++;
  if (triggeredTasks.removePoint == MAX_SCHEDULE_ENTRY) {
    triggeredTasks.removePoint = 0;
  }
  if (triggeredTasks.removePoint == triggeredTasks.insertPoint) {
    triggeredTasks.removePoint = -1;
  }
  pthread_mutex_unlock(&(triggeredTasks.taskEntryMutex));
  return;
}

/** Free the memory of the task.
 */
void freeSchedEntry(SCHED_ENTRY_t* schedEnt_p) {
  free(schedEnt_p->sch_task);
  free(schedEnt_p);
}


/** Insert the task to the triggeredTasks queue.
 */
void insertTriggeredTask(TM_TASK_t* task) {
  SCHED_ENTRY_t* schedEntry_p;

  pthread_mutex_lock(&(triggeredTasks.taskEntryMutex));
  if (triggeredTasks.insertPoint == triggeredTasks.removePoint) {
    // The queue is full. do nothing. Free the task.
    free(task);
    pthread_mutex_unlock(&(triggeredTasks.taskEntryMutex));
    return;
  }

  schedEntry_p = malloc(sizeof(SCHED_ENTRY_t));
  schedEntry_p->sch_task = task;
  schedEntry_p->sch_status = TASK_TRIGGERED;
  if (triggeredTasks.removePoint == -1) {
    triggeredTasks.removePoint = triggeredTasks.insertPoint;
  }
  triggeredTasks.table[triggeredTasks.insertPoint++] = schedEntry_p;
  if (triggeredTasks.insertPoint == MAX_SCHEDULE_ENTRY) {
    triggeredTasks.insertPoint = 0;
  }
  printf("Notify scheduler of new task.\n");
  printf("insertPoint = %d\n", triggeredTasks.insertPoint);
  printf("removePoint = %d\n", triggeredTasks.removePoint);
  pthread_cond_broadcast(&(triggeredTasks.newTaskCond));
  pthread_mutex_unlock(&(triggeredTasks.taskEntryMutex));
}


/** Move the tasks from the triggeredTasks table to the exeuctingTask
 *  table, set a timer at the deadline of the task, and call its fire
 *  method.
 */
void runTask(SCHED_ENTRY_t* schedEntry_p) {
  //SCHED_ENTRY_t* newTask;
  pthread_t executingThread;
  pthread_attr_t attr;
  int rc;

  if (!(schedEntry_p->sch_task)->hasDeadline) {
    // No monitoring thread.
    (schedEntry_p->sch_task)->exec();
    (schedEntry_p->sch_task)->produceOutput();
    removeNextTriggeredTask();
    freeSchedEntry(schedEntry_p);
  } else {
    // move the schedEntry_p.
    /*
    pthread_mutex_lock(&(executingTasks.taskListMutex));
    newTask = malloc(sizeof(TASK_LIST_t));
    newTask->entry = *task;
    newtask.next = NULL;
    newTask->previous = executingTasks.previous;
    executingTasks.previous->next = newTask;
    executingTasks.previous = newTask;
    pthread_mutex_unlock(&(executingTasks.taskListMutex));
    */
    removeNextTriggeredTask();
    pthread_mutex_lock(&schStatusMutex);
    schedEntry_p->sch_status = TASK_EXECUTING;
    pthread_mutex_unlock(&schStatusMutex);
    // create a thread that takes the task.
    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

    rc = pthread_create(&executingThread, &attr, monitorTask, (void *)schedEntry_p);
    if (rc) {
      printf("ERROR; return code from pthread_create() is %d\n", rc);
      exit(-1);
    }
    // Give a chance for the monitor thread to run.
    sched_yield();
    (schedEntry_p->sch_task)->exec();
    // Set the status of the task to be executing.
    pthread_mutex_lock(&schStatusMutex);
    schedEntry_p->sch_status = TASK_EXEC_DONE;
    pthread_mutex_unlock(&schStatusMutex);
    // Output will be produced in the monitoring thread.
    printf("Scheduler: Finished task without producing output.\n");
  }
}

void *monitorTask(void *entry_p) {
  SCHED_ENTRY_t* schedEntry_p = (SCHED_ENTRY_t*)entry_p;
  // getDeadline();
  struct timeval tmv;
  struct timeval deadline;
  long sleepSec;
  long sleepuSec;

  printf("In monitorTask \n");
  schedEntry_p->sch_task->getDeadline(&deadline);
  gettimeofday(&tmv, NULL);
  sleepuSec = deadline.tv_usec - tmv.tv_usec;
  sleepSec = deadline.tv_sec - tmv.tv_sec;
  if (sleepuSec < 0) {
    sleepuSec += 1e6;
    sleepSec--;
  }
  // sleep.
  if (sleepSec > 0) {
    sleep(sleepSec);
  } else if(sleepSec < 0) {
    // This should never happen. But we immdiate call stopExec.
    sleepuSec = 0;
  }
  usleep(sleepuSec);
  // Now we should stop firing.
  // yield wait
  // check status: if executing, call stopExec(), yield
  //               if exec_done, call produceOutput()
  pthread_mutex_lock(&schStatusMutex);
  if (schedEntry_p->sch_status != TASK_EXEC_DONE) {
    schedEntry_p->sch_task->stopExec();
    pthread_mutex_unlock(&schStatusMutex);
    sched_yield();

    while(1) {
      pthread_mutex_lock(&schStatusMutex);
      if (schedEntry_p->sch_status == TASK_EXEC_DONE) {
        pthread_mutex_unlock(&schStatusMutex);
        break;
      } else {
        pthread_mutex_unlock(&schStatusMutex);
        sched_yield();
      }
    }
  } else {
    pthread_mutex_unlock(&schStatusMutex);
  }
  // Already done. produce output.
  printf("monitor thread: produce output.\n");
  fflush(stdout);
  (schedEntry_p->sch_task)->produceOutput();
  // free the memory corresponding to the task.
  freeSchedEntry(schedEntry_p);

  /*
  pthread_mutex_lock(&(executingTasks.taskListMutex));
  task->next->previous = task->previous;
  task->previous->next = task->next;
  free(task);
  pthread_mutex_unlock(&(executingTasks.taskListMutex));
   */
  printf("monitor thread exits.\n");
  pthread_exit(NULL);
  return NULL;
}

void schedule() {
  SCHED_ENTRY_t* nextTask_p;
  while(1) {

    getNextTriggeredTask(&nextTask_p);
    if((nextTask_p->sch_task)->isReady()) {
      // move the task from triggeredTasks to executingTasks.
      // and the start the task.
      runTask(nextTask_p);
    } else {
      // remove the task from the triggeredTasks table.
      removeNextTriggeredTask();
      freeSchedEntry(nextTask_p);
    }
  }
}


void sched_init() {
  triggeredTasks.insertPoint = 0;
  triggeredTasks.removePoint = -1;
  pthread_mutex_init(&(triggeredTasks.taskEntryMutex), NULL);
  pthread_cond_init(&(triggeredTasks.newTaskCond),  NULL);
  pthread_mutex_init(&schStatusMutex, NULL);
}

int main() {
  sched_init();
  app_init();
  app_start();
  schedule();
  return 0;
}
