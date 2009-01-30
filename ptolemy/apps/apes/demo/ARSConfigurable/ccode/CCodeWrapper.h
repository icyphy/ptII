
#include "jni.h"

#ifndef _Included_HelloWorld
#define _Included_HelloWorld
typedef struct AA{
	JNIEnv *env; // corresponds to one particular java thread
	jobject obj;
} JAVAENV;

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT void JNICALL Java_ptolemy_apps_apes_AccessPointCallbackDispatcher_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CPUScheduler_InitializeC(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_EventManager_InitializeC(JNIEnv *env, jobject obj);

void callback(float , float );

#ifdef __cplusplus
}
#endif
#endif



