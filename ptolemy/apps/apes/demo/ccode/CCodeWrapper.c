 #include <jni.h>
 //#include <stdio.h>
 #include "CCodeWrapper.h"
#include"original.h"


/*****************************************************************************/

typedef struct AA{
	JNIEnv *env;
	jobject obj;
} JAVAENV;
 

JAVAENV envA, envB, envC;

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_CTaskA_CMethod(JNIEnv *env, jobject obj)
 {
	 envA.env=env;
	 envA.obj=obj;
     top8ms_offset0();
     return;
 }

/*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_CTaskB_CMethod(JNIEnv *env, jobject obj)
 {
	 envB.env=env;
	 envB.obj=obj;
     top8ms_offset4();
     return;
 }

 /*****************************************************************************/

 JNIEXPORT void JNICALL 
Java_ptolemy_apps_apes_demo_CTaskC_CMethod(JNIEnv *env, jobject obj)
 {
	 envC.env=env;
	 envC.obj=obj;
     eventf();
     return;
 }

 JNIEXPORT jint JNICALL Java_ptolemy_apps_apes_CTask_getG1(JNIEnv *env, jobject obj) {
	 return getG1();
 }
 JNIEXPORT jint JNICALL Java_ptolemy_apps_apes_CTask_getG2(JNIEnv *env, jobject obj) {
	 return getG2();
 }
 JNIEXPORT jint JNICALL Java_ptolemy_apps_apes_CTask_getG3(JNIEnv *env, jobject obj) {
	 return getG3();
 }
 /*****************************************************************************/

 void callback(char* fname, int port, float time) {

	 jclass cls=NULL;
	 jmethodID mid=NULL;
	 JAVAENV jenv;

	 if (!strcmp(fname, "top8ms_offset0")){
		 jenv=envA;
	 }

	 if (!strcmp(fname, "top8ms_offset4")){
		 jenv=envB;
	 }

	 if (!strcmp(fname, "eventf")){
		 jenv=envC;
	 }


	cls=(*(jenv.env))->GetObjectClass(jenv.env,jenv.obj);
	mid=(*(jenv.env))->GetMethodID(jenv.env, cls, "accessPointCallback", "()V");
	if (mid == NULL)
		return;
	(*(jenv.env))->CallVoidMethod(jenv.env, jenv.obj, mid, port, time);
 }

 /*****************************************************************************/
