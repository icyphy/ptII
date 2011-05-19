#include <stdio.h>
#include "jni.h"

#include "APESCodeWrapper.h"

/*****************************************************************************/
/*****************************************************************************/

 JavaVM *cached_jvm; 
 jobject dispatcher;
 jmethodID accessPointCallbackMethod, accessPointCallbackReturnValuesMethod;

/*****************************************************************************/
 JNIEXPORT jint JNICALL
 JNI_OnLoad(JavaVM *jvm, void *reserved)
 {
     JNIEnv *env;
     jclass cls;
     cached_jvm = jvm;  /* cache the JavaVM pointer */
 
 	 fprintf(stderr, "!!!!!!!\n");
	 fflush(stderr);
	 if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2)) {
         return JNI_ERR; /* JNI version not supported */
     }
	 
 	 fprintf(stderr, "!!!!!!!\n");
	 fflush(stderr);
	 cls = (*env)->FindClass(env, "ptolemy/apps/apes/AccessPointCallbackDispatcher");
     if (cls == NULL) {
         return JNI_ERR;
     } 
	 fprintf(stderr, "!!!!!!!\n");
	 fflush(stderr);
     accessPointCallbackMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DD)V");
     if (accessPointCallbackMethod == NULL) {
         return JNI_ERR;
     }
	 
	 accessPointCallbackReturnValuesMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DDLjava/lang/String;D)V");
     if (accessPointCallbackMethod == NULL) {
         return JNI_ERR;
     }

	 if(OS_JNI_OnLoad(env)){
         return JNI_ERR;
     } 
 

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
	 dispatcher = (*env)->NewWeakGlobalRef(env, obj); 
	 
     return;
 }
  
/*****************************************************************************/

 void callback(float exectime, float mindelay) {
	 JNIEnv *env = JNU_GetEnv();  
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackMethod, exectime, mindelay);    
 }
 
 /*****************************************************************************/
  void callbackV(float exectime, float mindelay, char* varName, double value) {
	 jmethodID method;
	 char buf[128];
	 jclass cls; 
	 JNIEnv *env = JNU_GetEnv();
	 
	 jstring string = (*env)->NewStringUTF(env, varName);  
	 
	 
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackReturnValuesMethod, exectime, mindelay, string, value);   

 }
 /*****************************************************************************/
 /*****************************************************************************/
