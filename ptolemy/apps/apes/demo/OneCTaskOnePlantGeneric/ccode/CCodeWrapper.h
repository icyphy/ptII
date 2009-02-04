
#include <jni.h>

#ifndef _Included_HelloWorld
#define _Included_HelloWorld
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL Java_ptolemy_apps_apes_AccessPointCallbackDispatcher_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CPUScheduler_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_EventManager_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_OSEKEntryPoint_InitializeC(JNIEnv *env, jobject obj);

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CTask_Cmethod(JNIEnv *, jobject, jstring taskName); 

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CTask_setGlobalVariable(JNIEnv *, jobject, jstring, double);  

void callback(double, double);
void callbackO(double exectime, double mindelay, char* varName, double value);
void callbackI(double exectime, double mindelay, char* varName);
void activateTask(int );
void terminateTask();

#ifdef __cplusplus
}
#endif
#endif



