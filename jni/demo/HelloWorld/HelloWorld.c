/* From http://www.inonit.com/cygwin/jni/helloWorld/c.html */
#include <stdio.h>

#include "HelloWorld.h"

JNIEXPORT void JNICALL Java_jni_demo_HelloWorld_HelloWorld_writeHelloWorldToStdout (JNIEnv *env, jclass c) {
    printf("Hello World!\n");
}
