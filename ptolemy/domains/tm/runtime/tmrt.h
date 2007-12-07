/** Copyright(c) 2002, Jie Liu
 *  ALl right reserved.
 *
 *  Timed-Multitasking Runtime
 *
 *  $Id$
 */

#ifndef __TMRT_H__
#define __TMRT_H__

#include "schedule.h"

void getNextTriggeredTask(SCHED_ENTRY_t** task);
void* monitorTask(void* schedEntry_p);
void removeNextTriggeredTask();
void insertTriggeredTask(TM_TASK_t* task);
void runTask(SCHED_ENTRY_t* schedEntry_p);
void schedule();
void freeSchedEntry(SCHED_ENTRY_t* schedEnt_p);

extern void app_init();
extern void app_start();

#endif
