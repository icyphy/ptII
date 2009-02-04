#ifndef _Included_APESCodeWrapper
#define _Included_APESCodeWrapper

#include "jni.h"
#include "OSEKCodeWrapper.h"

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL Java_ptolemy_apps_apes_AccessPointCallbackDispatcher_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CPUScheduler_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_EventManager_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CTask_Cmethod(JNIEnv *, jobject, jstring taskName); 

JNIEnv *JNU_GetEnv(void);

void callback(float , float );
void callbackO(float exectime, float mindelay, char* varName, double value);
void callbackI(float exectime, float mindelay, char* varName);

#ifdef __cplusplus
}
#endif
#endif



