
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

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskA_Cmethod(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskB_Cmethod(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskC_Cmethod(JNIEnv *, jobject); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSA_Cmethod(JNIEnv *, jobject); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSB_Cmethod(JNIEnv *, jobject); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSC_Cmethod(JNIEnv *, jobject); 

void callback(float , float );
void activateTask(int );
void terminateTask();

#ifdef __cplusplus
}
#endif
#endif



