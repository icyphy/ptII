
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

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_OneCTaskOnePlant_Task_Cmethod(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_OneCTaskOnePlant_Plant_Cmethod(JNIEnv *, jobject); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_OneCTaskOnePlant_IRSTask_Cmethod(JNIEnv *, jobject);  

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_OneCTaskOnePlant_Task_setLower(JNIEnv *, jobject, double); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_OneCTaskOnePlant_Task_setUpper(JNIEnv *, jobject, double); 

void callback(float , float);
void callbackV(float , float, char*, double);
void activateTask(int );
void terminateTask();

#ifdef __cplusplus
}
#endif
#endif



