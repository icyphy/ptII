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
//jclass _cls;


  JNIEXPORT void JNICALL 
          Java_ptolemy_domains_wireless_lib_tinyOS_VirtualTinyOS_initMote
          (JNIEnv *jni, jobject obj) {
      _jniEnv = (JNIEnv*) malloc(sizeof(JNIEnv));
      _jobject = (jobject) malloc(sizeof(jobject));
      //_cls = (jclass) malloc(sizeof(jclass));
      _jniEnv = jni;
      _jobject = obj;
      //_cls = (*_jniEnv)->GetObjectClass(_jniEnv, _jobject);
      initialize();
      start();        
  }

//extern "C"
  JNIEXPORT jint JNICALL 
          Java_ptolemy_domains_wireless_lib_tinyOS_VirtualTinyOS_signalTimerEvent
          (JNIEnv *jni, jobject obj) {
    _jniEnv = jni;
    _jobject = obj;
    Timer_Timer_fired();
    return 1;
  }
  
  void setupTimer(int period) {
    jclass cls = (*_jniEnv)->GetObjectClass(_jniEnv, _jobject);
    jmethodID mid = (*_jniEnv)->GetMethodID(_jniEnv, cls, "setupTimer", "(I)V");
    if (mid == 0) {
        printf("no method with name setupTimer.\n");
    }
    printf("get the method id with name setupTimer.\n");
    (*_jniEnv)->CallVoidMethod(_jniEnv, _jobject, mid, period);
  }
   
  void ledBlink(int x) {
    jclass cls = (*_jniEnv)->GetObjectClass(_jniEnv, _jobject);
    printf("call ledBlink of VirtualTinyOS.c and get the jclass. \n");
    jmethodID mid = (*_jniEnv)->GetMethodID(_jniEnv, cls, "ledBlink", "(I)V");
    if (mid == 0) {
        printf("no method with name ledBlink.\n");
    }
    printf("get the method id with name ledBlink.\n");
    (*_jniEnv)->CallVoidMethod(_jniEnv, _jobject, mid, x);
  }

