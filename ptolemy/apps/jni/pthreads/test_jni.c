#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <jni.h>
#include <pthread.h>

#include "Test.h"

pthread_t thread;

JavaVM *vm = NULL;
jobject obj;
static int end = 1;

static void *test_thread (void *arg)
{
    JNIEnv *env = NULL;
    jclass class;
    jmethodID callback;

    if ((*vm)->AttachCurrentThread (vm, (JNIEnv **) &env, NULL) < 0)
    {
        fprintf (stderr, "%s[%d]: AttachCurrentThread ()\n",
            __FILE__, __LINE__);
        return NULL;
    }

    class = (*env)->GetObjectClass (env, obj);

    callback = (*env)->GetMethodID (env, class, "callback", "()V");
    if (callback == NULL)
    {
        fprintf (stderr, "%s[%d]: GetMethodID ()\n",
            __FILE__, __LINE__);
        return NULL;
    }

    while (end)
    {
        (*env)->CallVoidMethod(env, obj, callback);
        sleep (1);
    }

    if ((*vm)->DetachCurrentThread (vm) < 0)
    {
        fprintf (stderr, "%s[%d]: DetachCurrentThread ()\n",
            __FILE__, __LINE__);
        return NULL;
    }

    return NULL;
}

JNIEXPORT void JNICALL
Java_Test_test_1start_1callback (JNIEnv *env, jobject object)
{
    int ret;

    end = 1;

    printf ("JNI: test_start_callback ()\n");

    if ((*env)->GetJavaVM (env, &vm) < 0)
    {
        fprintf (stderr, "%s[%d]: GetJavaVM ()\n",
            __FILE__, __LINE__);
        return;
    }

    obj = (*env)->NewGlobalRef (env, object);
    if (obj == NULL)
    {
        fprintf (stderr, "%s[%d]: NewGlobalRef ()\n",
            __FILE__, __LINE__);
        return;
    }

    if ((ret = pthread_create (&thread, NULL, test_thread, NULL)) != 0)
    {
        fprintf (stderr, "%s[%d]: pthread_create (): %s\n",
            __FILE__, __LINE__, strerror (ret));
        return;
    }
}

JNIEXPORT void JNICALL
Java_Test_test_1stop_1callback (JNIEnv *env, jobject object)
{
    int ret;

    printf ("JNI: test_stop_callback ()\n");

    end = 0;

    if ((ret = pthread_join (thread, NULL)) != 0)
    {
        fprintf (stderr, "%s[%d]: pthread_join (): %s\n",
            __FILE__, __LINE__, strerror (ret));
        return;
    }

    (*env)->DeleteGlobalRef (env, obj);
}
