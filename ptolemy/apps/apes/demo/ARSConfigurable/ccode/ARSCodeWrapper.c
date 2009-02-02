#include "jni.h"
#include <stdio.h>
#include "APESCodeWrapper.h"
#include "ARSCodeWrapper.h"
#include "ARSMain.h"
#include "dynamicsControll_ert_rtw/dynamicsControll.h" 


/*****************************************************************************/
 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_CTask_CMethod(JNIEnv *env, jobject obj, jstring taskName)
 {
	 const char *task;
 
	 task = (*env)->GetStringUTFChars(env, taskName, 0); 

	 if (strcmp(task, "DispatcherTask") == 0) 
		appDispatcher();
	 else if (strcmp(task, "DynamicsControllerTask") == 0)
		dynaController();
	 else if (strcmp(task, "MotorControllerTask") == 0)
		motorController();
	 else if (strcmp(task, "DispatcherIRS") == 0) 
		dispatcherIRS();
	(*env)->ReleaseStringUTFChars(env, taskName, task);
 }

 /*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DynamicsControllerTask_CMethod(JNIEnv *env, jobject obj)
 { 
     dynamicsControll_step();
     return;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_MotorControllerTask_CMethod(JNIEnv *env, jobject obj)
 { 
     motorController();
     return;
 }

 /*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DispatcherTask_CMethod(JNIEnv *env, jobject obj)
 { 
     appDispatcher();
     return;
 }

 /*****************************************************************************/
  
  JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_ARS_DispatcherIRS_CMethod(JNIEnv *env, jobject obj)
 {  
	 dispatcherIRS();
     return;
 }
 
 /*****************************************************************************/

 