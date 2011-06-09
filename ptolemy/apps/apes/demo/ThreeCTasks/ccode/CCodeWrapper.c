 #include <jni.h>
 #include <stdio.h>
 #include "CCodeWrapper.h"
#include"original.h"


/*****************************************************************************/

typedef struct AA{
	JNIEnv *env; // corresponds to one particular java thread
	jobject obj;
} JAVAENV;
 

JAVAENV currentEnv; // assuming only one thread is active in this piece of C-Code at a time


 JavaVM *cached_jvm; 
 jobject dispatcher, osekEntryPoint;
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
	 
	 cls = (*env)->FindClass(env, "ptolemy/apps/apes/OSEKEntryPoint");
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
	 jclass apcd; 
	 fprintf(stderr, "AccessPointDispatcher_Initialize ");  
	 dispatcher = (*env)->NewWeakGlobalRef(env, obj); 
	 
     return;
 }

   JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_OSEKEntryPoint_InitializeC(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "OSEKEntryPoint_Initialize ");
	 osekEntryPoint = (*env)->NewWeakGlobalRef(env, obj);  
     return;
 }
/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskA_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "CTaskA ");
	 currentEnv.env = env; currentEnv.obj = obj;
     top8ms_offset0();
     return;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskB_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "CTaskB ");
	 currentEnv.env = env; currentEnv.obj = obj;
     top8ms_offset4();
     return;
 }

 /*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_CTaskC_CMethod(JNIEnv *env, jobject obj)
 {
	 fprintf(stderr, "CTaskC ");
	 currentEnv.env = env; currentEnv.obj = obj;
     eventf();
     return;
 }

 /*****************************************************************************/
  
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSA_CMethod(JNIEnv *env, jobject obj)
 { 
	 fprintf(stderr, "IRSA ");
	 currentEnv.env = env; currentEnv.obj = obj;
	 callback(-1, 0);
	 activateTask(1);
	 callback(0.2, 0);
	 terminateTask();
     return;
 }
 
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSB_CMethod(JNIEnv *env, jobject obj)
 { 
	 fprintf(stderr, "IRSB ");
	 currentEnv.env = env; currentEnv.obj = obj;
	 callback(-1, 0);
	 activateTask(2);
	 callback(0.2, 0);
	 terminateTask();
     return;
 }
 
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ThreeCTasks_IRSC_CMethod(JNIEnv *env, jobject obj)
 { 
	 fprintf(stderr, "IRSC ");
	 currentEnv.env = env; currentEnv.obj = obj;
	 callback(-1, 0);
	 activateTask(3);
	 callback(0.2, 0);
	 terminateTask();
     return;
 }


 /*****************************************************************************/

 void callback(float exectime, float mindelay) {
	 jmethodID method;
	 jclass cls; 
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "callback ");  
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackMethod, exectime, mindelay);   
	 fprintf(stderr, "after callback ");
 }
 
 void activateTask(int taskId) {
	 jmethodID method;
	 jclass cls;
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "activateTask ");   
	 (*(env))->CallIntMethod(env, osekEntryPoint, activateTaskMethod, taskId);   
	 fprintf(stderr, "activateTask done ");
 }
 
  void terminateTask() {
	 jmethodID method;
	 jclass cls;
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "terminateTask ");   
	 (*(env))->CallVoidMethod(env, osekEntryPoint, terminateTaskMethod);  
	 fprintf(stderr, "terminate done ");
 }

 /*****************************************************************************/
