/* VirtualTinyOS.c - Java Native Interface. This only has part of the required methods now...


Authors: Yang
Version $$
*/

#include <jni.h>
#include "VirtualTinyOS.h"
#include "Timer.h"
#include "BlinkMain.h"
#include <stdio.h>
#include <stdlib.h>

JNIEnv* _jniEnv;
jobject _jobject;


  JNIEXPORT void JNICALL 
          Java_ptolemy_domains_wireless_lib_tinyOS_VirtualTinyOS_initMote
          (JNIEnv *jni, jobject obj) {
      _jniEnv = (JNIEnv*) malloc(sizeof(JNIEnv));
      _jobject = (jobject) malloc(sizeof(jobject));
      _jniEnv = jni;
      _jobject = obj;
      initialize();
      start();        
  }

//extern "C"
  JNIEXPORT jint JNICALL 
          Java_ptolemy_domains_wireless_lib_tinyOS_VirtualTinyOS_signalTimerEvent
          (JNIEnv *jni, jobject obj) {
    Timer_Timer_fired();
    return 1;
  }
  
  void setupTimer(int period) {
    jclass cls = (*_jniEnv)->GetObjectClass(_jniEnv, _jobject);
    jmethodID mid = (*_jniEnv)->GetMethodID(_jniEnv, cls, "setupTimer", "(I)V");
    if (mid == 0) {
        printf("no method with name LedDisplay.\n");
    }
    printf("get the method id with name setupTimer.\n");
    (*_jniEnv)->CallVoidMethod(_jniEnv, _jobject, mid, period);
  }
   


