#include <stdio.h>
#include "jni.h"

#include "APESCodeWrapper.h"

/*****************************************************************************/
/*****************************************************************************/

 JavaVM *cached_jvm; 
 jobject dispatcher;
 jmethodID accessPointCallbackMethod;

/*****************************************************************************/
 JNIEXPORT jint JNICALL
 JNI_OnLoad(JavaVM *jvm, void *reserved)
 {
     JNIEnv *env;
     jclass cls;
     cached_jvm = jvm;  /* cache the JavaVM pointer */
 
     if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2)) {
         return JNI_ERR; /* JNI version not supported */
     }
	 
     cls = (*env)->FindClass(env, "ptolemy/apps/apes/AccessPointCallbackDispatcher");
     if (cls == NULL) {
         return JNI_ERR;
     } 
     accessPointCallbackMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DD)V");
     if (accessPointCallbackMethod == NULL) {
         return JNI_ERR;
     }
	 
	 if(!OS_JNI_OnLoad(env)){
         return JNI_ERR;
     } 
 
	 fprintf(stderr, "!!!!!!!\n");
     return JNI_VERSION_1_2;
 }
/*****************************************************************************/

 JNIEnv *JNU_GetEnv()
 {
     JNIEnv *env;
     (*cached_jvm)->GetEnv(cached_jvm,
                           (void **)&env,
                           JNI_VERSION_1_2);
     return env;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_AccessPointCallbackDispatcher_InitializeC(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "AccessPointDispatcher_Initialize ");  
	 dispatcher = (*env)->NewWeakGlobalRef(env, obj); 
	 
     return;
 }
  
/*****************************************************************************/

 void callback(float exectime, float mindelay) {
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "callback ");  
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackMethod, exectime, mindelay);   
	 fprintf(stderr, "after callback ");
 }
 
 /*****************************************************************************/
