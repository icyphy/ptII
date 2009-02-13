#include <stdio.h>
#include <stdarg.h>
#include "jni.h"
#include "APESCodeWrapper.h"
#include "OSEK_types.h"
#include "ARSmain.h"


/*****************************************************************************/
/*****************************************************************************/

 jobject osekEntryPoint;
 jmethodID activateTaskMethod, terminateTaskMethod, setEventMethod, waitEventMethod, clearEventMethod;
 
 int OS_JNI_OnLoad(JNIEnv *env)
 {
 
     jclass cls;
 
	 cls = (*env)->FindClass(env, "ptolemy/apps/apes/OSEKEntryPoint");
     if (cls == NULL) {
         return 1;
     } 

     activateTaskMethod = (*env)->GetMethodID(env, cls, "activateTask", "(I)I");
     if (activateTaskMethod == NULL) {
         return 1;
     }
	 terminateTaskMethod = (*env)->GetMethodID(env, cls, "terminateTask", "()V");
     if (terminateTaskMethod == NULL) {
         return 1;
     }
     setEventMethod = (*env)->GetMethodID(env, cls, "setEvent", "(II)I");
     if (setEventMethod == NULL) {
         return 1;
     }
	 waitEventMethod = (*env)->GetMethodID(env, cls, "waitEvent", "(I)I");
     if (waitEventMethod == NULL) {
         return 1;
     }
	 clearEventMethod = (*env)->GetMethodID(env, cls, "clearEvent", "(I)I");
     if (clearEventMethod == NULL) {
         return 1;
     }

     return 0;
 }
/*****************************************************************************/
 
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_OSEKEntryPoint_InitializeC(JNIEnv *env, jobject obj)
 {
	 osekEntryPoint = (*env)->NewWeakGlobalRef(env, obj); 
	 if (appStartup()){ 
	 }
 }
/*****************************************************************************/

   /* OSEK System calls */
/*****************************************************************************/

 void ActivateTask(TaskType taskId) {
	 JNIEnv *env = JNU_GetEnv(); 
	 (*(env))->CallIntMethod(env, osekEntryPoint, activateTaskMethod, taskId);    
 }
 
/*****************************************************************************/

  void TerminateTask() {
	 JNIEnv *env = JNU_GetEnv();  
	 (*(env))->CallVoidMethod(env, osekEntryPoint, terminateTaskMethod);  
  }

/*****************************************************************************/
 void SetEvent(TaskType taskId, EventMaskType eventMask) {
	 JNIEnv *env = JNU_GetEnv(); 
	 (*(env))->CallIntMethod(env, osekEntryPoint, setEventMethod, taskId, eventMask);   
 }

/*****************************************************************************/
 void WaitEvent(EventMaskType eventMask) {
	 JNIEnv *env = JNU_GetEnv(); 
	 (*(env))->CallIntMethod(env, osekEntryPoint, waitEventMethod, eventMask);   
}

/*****************************************************************************/
 void ClearEvent(EventMaskType eventMask) {
	 JNIEnv *env = JNU_GetEnv(); 
	 (*(env))->CallIntMethod(env, osekEntryPoint, clearEventMethod, eventMask);   
 }

/*****************************************************************************/

/* void systemCall(int nArgs, jmethodID jMethod,...) {
	 va_list pList;
 	 JNIEnv *env = JNU_GetEnv();
 
	 va_start(pList, jMethod);

	 switch(nArgs){
	 case 0:
		 (*(env))->CallIntMethod(env, osekEntryPoint, jMethod);
		 break;
	 case 1:
		 (*(env))->CallIntMethod(env, osekEntryPoint, jMethod, va_arg(pList, int));
		 break;
	 case 2:
		 (*(env))->CallIntMethod(env, osekEntryPoint, jMethod, va_arg(pList, int), va_arg(pList, int));
		 break;
	 }
	 va_end(pList);
 }*/

/*****************************************************************************/
/*****************************************************************************/
