#ifndef _Included_OSEKCodeWrapper
#define _Included_OSEKCodeWrapper

#include "jni.h"
#include "OSEK_types.h"

/*
#define ActivateTask(X)		systemCall(1,activateTaskMethod,X)
#define TerminateTask()		systemCall(0,terminateTaskMethod) 
#define SetEvent(X,Y)		systemCall(2,setEventMethod,X,Y) 
#define WaitEvent(X)		systemCall(1,waitEventMethod,X) 
#define ClearEvent(X)		systemCall(1,clearEventMethod,X)
*/

extern jmethodID activateTaskMethod, terminateTaskMethod, setEventMethod, waitEventMethod, clearEventMethod;

extern int appStartup(void);

int OS_JNI_OnLoad(JNIEnv *env);

void systemCall(int n_args, jmethodID jMethod,...);


void ActivateTask(TaskType taskId);
void TerminateTask();
void SetEvent(TaskType, EventMaskType);
void WaitEvent(EventMaskType);
void ClearEvent(EventMaskType);


#endif



