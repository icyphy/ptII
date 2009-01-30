#include "jni.h"
#include <stdio.h>
#include "CCodeWrapper.h"
#include "ARSMain.h"



/*****************************************************************************/

typedef struct AA{
	JNIEnv *env; // corresponds to one particular java thread
	jobject obj;
} JAVAENV;
 

JAVAENV currentEnv; // assuming only one thread is active in this piece of C-Code at a time


 JavaVM *cached_jvm; 
 jobject dispatcher, cpuScheduler, eventManager;
 jclass cpus, apcd;
 jmethodID accessPointCallbackMethod, activateTaskMethod, terminateTaskMethod;
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
	 apcd = (*env)->NewWeakGlobalRef(env, cls);
     if (apcd == NULL) {
         return JNI_ERR;
     }
     accessPointCallbackMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DD)V");
     if (accessPointCallbackMethod == NULL) {
         return JNI_ERR;
     }
	 
	 cls = (*env)->FindClass(env, "ptolemy/apps/apes/CPUScheduler");
     if (cls == NULL) {
         return JNI_ERR;
     } 
	 cpus = (*env)->NewWeakGlobalRef(env, cls);
     if (cpus == NULL) {
         return JNI_ERR;
     }
     activateTaskMethod = (*env)->GetMethodID(env, cls, "activateTask", "(I)I");
     if (activateTaskMethod == NULL) {
         return JNI_ERR;
     }
	 terminateTaskMethod = (*env)->GetMethodID(env, cls, "terminateTask", "()V");
     if (terminateTaskMethod == NULL) {
         return JNI_ERR;
     }
	  
	 fprintf(stderr, "!!!!!!!\n");
     return JNI_VERSION_1_2;
 }
 
  JNIEnv *JNU_GetEnv()
 {
     JNIEnv *env;
     (*cached_jvm)->GetEnv(cached_jvm,
                           (void **)&env,
                           JNI_VERSION_1_2);
     return env;
 }

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_AccessPointCallbackDispatcher_InitializeC(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "AccessPointDispatcher_Initialize ");  
	 dispatcher = (*env)->NewWeakGlobalRef(env, obj); 
	 
     return;
 }
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_CPUScheduler_InitializeC(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "CPUScheduler_Initialize "); 
	 cpuScheduler = (*env)->NewWeakGlobalRef(env, obj); 

     return;
 }
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_EventManager_InitializeC(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "EventManager_Initialize ");
	 eventManager = (*env)->NewWeakGlobalRef(env, obj);  
     return;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DynamicsControllerTask_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "DynamicsControllerTask ");
	 currentEnv.env = env; currentEnv.obj = obj;
     dynamicsControll_step();
     return;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_MotorControllerTask_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "MotorControllerTask ");
	 currentEnv.env = env; currentEnv.obj = obj;
     MotorController_step();
     return;
 }

 /*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DispatcherTask_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "DispatcherTask ");
	 currentEnv.env = env; currentEnv.obj = obj;
     dispatcherTask();
     return;
 }

 /*****************************************************************************/
  
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DispatcherIRS_CMethod(JNIEnv *env, jobject obj)
 { 
	 fprintf(stderr, "IRSA ");
	 currentEnv.env = env; currentEnv.obj = obj;
	 callback(-1, 0);
	 activateTask(1);
	 callback(0.2, 0);
	 terminateTask();
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

  /* OSEK System calls */
 void ActivateTask(int taskId) {
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "activateTask ");   
	 (*(env))->CallIntMethod(env, cpuScheduler, activateTaskMethod, taskId);   
	 fprintf(stderr, "ActivateTask done ");
 }
 
 void TerminateTask() {
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "terminateTask ");   
	 (*(env))->CallVoidMethod(env, cpuScheduler, terminateTaskMethod);  
	 fprintf(stderr, "TerminateTask done ");
 }

 void SetEvent(int taskId, ) {
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "terminateTask ");   
	 (*(env))->CallVoidMethod(env, cpuScheduler, terminateTaskMethod);  
	 fprintf(stderr, "TerminateTask done ");
 }

 /*****************************************************************************/
