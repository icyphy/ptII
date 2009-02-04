 #include <jni.h>
 #include <stdio.h>
 #include "CCodeWrapper.h"
#include"original.h"


/*****************************************************************************/

typedef struct AA{
	JNIEnv *env; // corresponds to one particular java thread
	jobject obj;
} JAVAENV;


 JavaVM *cached_jvm; 
 jobject dispatcher, cpuScheduler, eventManager, osekEntryPoint;
 jclass cpus, apcd;
 jmethodID accessPointCallbackMethod, activateTaskMethod, terminateTaskMethod, accessPointCallbackReturnValuesMethod, accessPointCallbackInputValuesMethod;
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
	 accessPointCallbackReturnValuesMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DDLjava/lang/String;D)V");
     if (accessPointCallbackReturnValuesMethod == NULL) {
         return JNI_ERR;
     }
	 
	 accessPointCallbackInputValuesMethod = (*env)->GetMethodID(env, cls, "accessPointCallback", "(DDLjava/lang/String;)V");
     if (accessPointCallbackInputValuesMethod == NULL) {
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
 
 JNIEXPORT void JNICALL Java_ptolemy_apps_apes_CTask_setGlobalVariable(JNIEnv *env, jobject obj, jstring string, double value) {
	char *varName = (*env)->GetStringUTFChars(env, string, 0);

	setGlobalVariable(varName, value);
 }  

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_CTask_CMethod(JNIEnv *env, jobject obj, jstring taskName)
 {
	 fprintf(stderr, "CTask /// ");
	 const char *taskN = (*env)->GetStringUTFChars(env, taskName, 0);
	 fprintf(stderr, taskN);
	 if (strcmp(taskN, "Task") == 0) 
		task();
	 else if (strcmp(taskN, "IRST") == 0) 
		irs();
		
 }




 void callback(double exectime, double mindelay) {
	 jmethodID method;
	 jclass cls; 
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "callback ");  
	 fprintf(stderr, "%f ", exectime);  
	 fprintf(stderr, "%f ", mindelay); 
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackMethod, exectime, mindelay);   
	 fprintf(stderr, "after callback ");
 }
 
  void callbackO(double exectime, double mindelay, char* varName, double value) {
	 jmethodID method;
	 char buf[128];
	 jclass cls; 
	 JNIEnv *env = JNU_GetEnv();
	 fprintf(stderr, "callback ");  
	 
	 jstring string = (*env)->NewStringUTF(env, varName);  
	 
	 
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackReturnValuesMethod, exectime, mindelay, string, value);   
	 fprintf(stderr, "after callback ");
 }
 
    void callbackI(double exectime, double mindelay, char* varName) {
	 jmethodID method;
	 char buf[128];
	 jclass cls; 
	 JNIEnv *env = JNU_GetEnv();
	 
	 jstring string = (*env)->NewStringUTF(env, varName);  
	 
	 
	 (*(env))->CallVoidMethod(env, dispatcher, accessPointCallbackInputValuesMethod, exectime, mindelay, string);   

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
