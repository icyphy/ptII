#ifndef _Included_ARSCodeWrapper
#define _Included_ARSCodeWrapper

#include "jni.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ARS_DynamicsControllerTask_Cmethod(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ARS_MotorControllerTask_Cmethod(JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ARS_DispatcherTask_Cmethod(JNIEnv *, jobject); 
JNIEXPORT void JNICALL Java_ptolemy_apps_apes_demo_ARS_DispatcherIRS_Cmethod(JNIEnv *, jobject); 

#ifdef __cplusplus
}
#endif
#endif



