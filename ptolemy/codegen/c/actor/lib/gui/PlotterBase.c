/***preinitBlock***/
#ifndef _JAVA_INVOCATION_INTERFACE_PREINIT
#define _JAVA_INVOCATION_INTERFACE_PREINIT

#ifdef __MAC_OS_X_VERSION_10_0
/* Mac OS X code based on simple.c from http://developer.apple.com/samplecode/simpleJavaLauncher/simpleJavaLauncher.html
which has this license:
        Disclaimer:        IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc.
                                ("Apple") in consideration of your agreement to the following terms, and your
                                use, installation, modification or redistribution of this Apple software
                                constitutes acceptance of these terms.  If you do not agree with these terms,
                                please do not use, install, modify or redistribute this Apple software.

                                In consideration of your agreement to abide by the following terms, and subject
                                to these terms, Apple grants you a personal, non-exclusive license, under Apple’s
                                copyrights in this original Apple software (the "Apple Software"), to use,
                                reproduce, modify and redistribute the Apple Software, with or without
                                modifications, in source and/or binary forms; provided that if you redistribute
                                the Apple Software in its entirety and without modifications, you must retain
                                this notice and the following text and disclaimers in all such redistributions of
                                the Apple Software.  Neither the name, trademarks, service marks or logos of
                                Apple Computer, Inc. may be used to endorse or promote products derived from the
                                Apple Software without specific prior written permission from Apple.  Except as
                                expressly stated in this notice, no other rights or licenses, express or implied,
                                are granted by Apple herein, including but not limited to any patent rights that
                                may be infringed by your derivative works or by other works in which the Apple
                                Software may be incorporated.

                                The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO
                                WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED
                                WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR
                                PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN
                                COMBINATION WITH YOUR PRODUCTS.

                                IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR
                                CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
                                GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
                                ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION
                                OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT
                                (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
                                ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


*/

#include <sys/stat.h>
#include <sys/resource.h>
#include <pthread.h>
#include <CoreFoundation/CoreFoundation.h>
#include <sys/types.h>
#include <unistd.h>
#include <JavaVM/jni.h>

/* This structure will be used to store the options, args, */
/* and main class needed to invoke this application */
typedef struct {
    void *                options;
    int                        nOptions;
    char *                 mainClass;
    char **                args;
    int                        numberOfArgs;
} VMLaunchOptions;

/* Parses command line options for the VM options, properties,
   main class, and main class args and returns them in the VMLaunchOptions
   structure.
*/
extern VMLaunchOptions * NewVMLaunchOptions(int argc, const char **argv);

/* Release the Memory used by the VMLaunchOptions */
void freeVMLaunchOptions( VMLaunchOptions * vmOptionsPtr);

/*
 * Returns a new array of Java string objects for the specified
 * array of platform strings.
 */
extern jobjectArray NewPlatformStringArray(JNIEnv *env, char **strv, int strc);

/* Sets the applications name for the application menu */
extern void setAppName(const char * name);

static void* startupJava(void *options);

/* call back for dummy source used to make sure the CFRunLoop doesn't exit right away */
/* This callback is called when the source has fired. */
void sourceCallBack (  void *info  ) {}

/*
   Parses command line options for the VM options, properties,
   main class, and main class args and returns them in the VMLaunchOptions
   structure.
*/
VMLaunchOptions * NewVMLaunchOptions(int argc, const char **currentArg)
{
    int ArgIndex = 0;

    /* The following Strings are used to convert the command line -cp to -Djava.class.path= */
    CFStringRef classPathOption = CFSTR("-cp");
    CFStringRef classPathDefine = CFSTR("-Djava.class.path=");

    /* vmOptionsCFArrayRef will temporarly hold a list of VM options and properties to be passed in when
       creating the JVM
    */
    CFMutableArrayRef vmOptionsCFArrayRef = CFArrayCreateMutable(NULL,0,&kCFTypeArrayCallBacks);

    /* mainArgsCFArrayRef will temporarly hold a list of arguments to be passed to the main method of the
       main class
    */
    CFMutableArrayRef mainArgsCFArrayRef = CFArrayCreateMutable(NULL,0,&kCFTypeArrayCallBacks);

    /* Allocated the structure that will be used to return the launch options */
    VMLaunchOptions * vmLaunchOptions = malloc(sizeof(VMLaunchOptions));

    /* Start with the first arg, not the path to the tool */
    ArgIndex++;
    currentArg++;

    /* JVM options start with - */
    while(ArgIndex < argc && **currentArg == '-') {
        CFMutableStringRef option = CFStringCreateMutable(NULL, 0);
        CFStringAppendCString(option, *currentArg, kCFStringEncodingUTF8);

        /* If the option string is '-cp', replace it with '-Djava.class.path=' and append
            then next option which contains the actuall class path.
        */
        CFRange rangeToSearch = CFRangeMake(0,CFStringGetLength(option));
        if (CFStringFindAndReplace(option, classPathOption, classPathDefine, rangeToSearch, kCFCompareAnchored) != 0) {
            /* So the option was -cp, and we replaced it with -Djava.class.path= */
            /* Now append the next option which is the actuall class path */
            currentArg++;
            ArgIndex++;
            if (ArgIndex < argc) {
                CFStringAppendCString(option, *currentArg, kCFStringEncodingUTF8);
            } else {
                /* We shouldn't reach here unless the last arg was -cp */
                fprintf(stderr, "[JavaAppLauncher Error] Error parsing class path.\n");
                /* Release the option CFString heresince the break; statement is going */
                /* to skip the release in this loop */
                CFRelease(option);
                break;
            }
        }

        /* Add this to our list of JVM options */
        CFArrayAppendValue(vmOptionsCFArrayRef,option);
        /* When an object is added to a CFArray the array retains a reference to it, this means */
        /* we need to release the object so that the memory will be freed when we release the CFArray. */
        CFRelease(option);

        /* On to the next one */
        currentArg++;
        ArgIndex++;
    }

    /* Now we know how many JVM options there are and they are all in a CFArray of CFStrings. */
    vmLaunchOptions->nOptions = CFArrayGetCount(vmOptionsCFArrayRef);
    /* We only need to do this if there are options */
    if ( vmLaunchOptions->nOptions > 0) {
        int index;
        /* Allocate some memory for the array of JavaVMOptions */
        JavaVMOption * option = malloc(vmLaunchOptions->nOptions*sizeof(JavaVMOption));
        vmLaunchOptions->options = option;

        /* Itterate over each option adding it to the JavaVMOptions array */
        for(index = 0;index < vmLaunchOptions->nOptions; index++, option++) {
            /* Allocate enough memory for each optionString char* to hold the max possible lengh a UTF8 */
            /* encoded copy of the string would require */
            CFStringRef optionStringRef = (CFStringRef)CFArrayGetValueAtIndex(vmOptionsCFArrayRef,index);
            CFIndex optionStringSize = CFStringGetMaximumSizeForEncoding(CFStringGetLength(optionStringRef), kCFStringEncodingUTF8);
            option->extraInfo = NULL;
            option->optionString = malloc(optionStringSize+1);
            /* Now copy the option into the the optionString char* buffer in a UTF8 encoding */
            if (!CFStringGetCString(optionStringRef, (char *)option->optionString, optionStringSize, kCFStringEncodingUTF8)) {
                fprintf(stderr, "[JavaAppLauncher Error] Error parsing JVM options.\n");
                exit(-1);
            }
        }

    } else {
        vmLaunchOptions->options = NULL;
    }

    /* Now we know how many args for main there are and they are all in a CFArray of CFStrings. */
    vmLaunchOptions->numberOfArgs = CFArrayGetCount(mainArgsCFArrayRef);
    /* We only need to do this if there are args */
    if ( vmLaunchOptions->numberOfArgs > 0) {
        int index;
        char ** arg;
        /* Allocate some memory for the array of char *'s */
        vmLaunchOptions->args = malloc(vmLaunchOptions->numberOfArgs*sizeof(char *));

        for(index = 0, arg = vmLaunchOptions->args;index < vmLaunchOptions->numberOfArgs; index++, arg++)
        {
            /* Allocate enough memory for each arg char* to hold the max possible lengh a UTF8 */
            /* encoded copy of the string would require */
            CFStringRef argStringRef = (CFStringRef)CFArrayGetValueAtIndex(mainArgsCFArrayRef,index);
            CFIndex argStringSize = CFStringGetMaximumSizeForEncoding(CFStringGetLength(argStringRef), kCFStringEncodingUTF8);
            *arg = (char*)malloc(argStringSize+1);
            /* Now copy the arg into the the args char* buffer in a UTF8 encoding */
            if (!CFStringGetCString(argStringRef, *arg, argStringSize, kCFStringEncodingUTF8)) {
                fprintf(stderr, "[JavaAppLauncher Error] Error parsing args.\n");
                exit(-1);
            }
        }

    } else {
        vmLaunchOptions->args = NULL;
    }
    /* Free the Array's holding our options and args */
    /* Releaseing an array also releases its references to the objects it contains */
    CFRelease(vmOptionsCFArrayRef);
    CFRelease(mainArgsCFArrayRef);
    return vmLaunchOptions;
}

/* Release the Memory used by the VMLaunchOptions */
void freeVMLaunchOptions( VMLaunchOptions * vmOptionsPtr) {
    int index = 0;
    if (vmOptionsPtr != NULL) {
        JavaVMOption * option = vmOptionsPtr->options;
        char ** arg = vmOptionsPtr->args;

        /* Itterate through the JVM options, freeing the optionStrings, */
        /* and extraInfo. */
        if (option != NULL) {
            for(index = 0; index < vmOptionsPtr->nOptions; index++,option++) {
                if (option->optionString != NULL)
                    free(option->optionString);

                if (option->extraInfo != NULL)
                    free(option->extraInfo);
            }
            free(vmOptionsPtr->options);
        }

        /* Itterate through the args for main, freeing each arg string. */
        if (arg != NULL) {
            for(index = 0; index < vmOptionsPtr->numberOfArgs; index++,option++,arg++) {
                if (*arg != NULL)
                    free(*arg);
            }
            free(vmOptionsPtr->args);
        }
        free(vmOptionsPtr);
    }
}

/* setting the environment varible APP_NAME_<pid> to the applications name */
/* sets it for the application menu */
void setAppName(const char * name) {
    char a[32];
    pid_t id = getpid();
    sprintf(a,"APP_NAME_%ld",(long)id);
    setenv(a, name, 1);
}

/*
 * Returns a new array of Java string objects for the specified
 * array of platform strings.
 */
jobjectArray
NewPlatformStringArray(JNIEnv *env, char **strv, int strc)
{
    jarray cls;
    jarray ary = NULL;
    int i;

    /* Look up the String class */
    cls = (*env)->FindClass(env, "java/lang/String");
    if (cls != NULL) {
        /* Create a new arrary with strc elements */
        ary = (*env)->NewObjectArray(env, strc, cls, 0);
        if (ary != NULL)
            /* Add each of the c strings to the new array as
               UTF java.lang.String objects */
            for (i = 0; i < strc; i++) {
                jstring str = (*env)->NewStringUTF(env, *strv++);
                if (str != NULL) {
                    (*env)->SetObjectArrayElement(env, ary, i, str);
                    /*The array now holds a reference to then string
                      so we can delete ours */
                    (*env)->DeleteLocalRef(env, str);
                } else {
                    break;
                }
            }
    }
    return ary;
}

#endif // __MAC_OS_X_VERSION_10_0


JavaVM* jvm;
JNIEnv* env;
JavaVMInitArgs args;
JavaVMOption options[1];
#endif

jclass $actorSymbol(plotClass);
jobject $actorSymbol(plotObject);
jmethodID $actorSymbol(plotConstructor);
jmethodID $actorSymbol(plotAddPoint);

jclass $actorSymbol(plotMLApplicationClass);
jobject $actorSymbol(plotMLApplicationObject);
jmethodID $actorSymbol(plotMLApplicationConstructor);

jclass $actorSymbol(plotMLParserClass);
jobject $actorSymbol(plotMLParserObject);
jmethodID $actorSymbol(plotMLParserConstructor);
jmethodID $actorSymbol(plotMLParserParse);
/**/

/***createJVMBlock($path)***/
#ifndef _JAVA_INVOCATION_INTERFACE_INIT
#define _JAVA_INVOCATION_INTERFACE_INIT

#ifdef __MAC_OS_X_VERSION_10_0
int result = 0;
JavaVM* theVM;

VMLaunchOptions * launchOptions = (VMLaunchOptions*)options;

/*
To invoke Java 1.4.1 or the currently preferred JDK as defined by the operating system (1.4.2 as of the release of this sample and the release of Mac OS X 10.4) nothing changes in 10.4 vs 10.3 in that when a JNI_VERSION_1_4 is passed into JNI_CreateJavaVM as the args.version it returns the current preferred JDK.

To specify the current preferred JDK in a family of JVM's, say the 1.5.x family, applications should set the environment variable JAVA_JVM_VERSION to 1.5, and then pass JNI_VERSION_1_4 into JNI_CreateJavaVM as the args.version. To get a specific Java 1.5 JVM, say Java 1.5.0, set the environment variable JAVA_JVM_VERSION to 1.5.0. For Java 1.6 it will be the same in that applications will need to set the environment variable JAVA_JVM_VERSION to 1.6 to specify the current preferred 1.6 Java VM, and to get a specific Java 1.6 JVM, say Java 1.6.1, set the environment variable JAVA_JVM_VERSION to 1.6.1.

To make this sample bring up the current preferred 1.5 JVM, set the environment variable JAVA_JVM_VERSION to 1.5 before calling JNI_CreateJavaVM as shown below.  Applications must currently check for availability of JDK 1.5 before requesting it.  If your application requires JDK 1.5 and it is not found, it is your responsibility to report an error to the user. To verify if a JVM is installed, check to see if the symlink, or directory exists for the JVM in /System/Library/Frameworks/JavaVM.framework/Versions/ before setting the environment variable JAVA_JVM_VERSION.

If the environment variable JAVA_JVM_VERSION is not set, and JNI_VERSION_1_4 is passed into JNI_CreateJavaVM as the args.version, JNI_CreateJavaVM will return the current preferred JDK. Java 1.4.2 is the preferred JDK as of the release of this sample and the release of Mac OS X 10.4.
*/
{
    CFStringRef targetJVM = CFSTR("1.5");
    CFBundleRef JavaVMBundle;
    CFURLRef    JavaVMBundleURL;
    CFURLRef    JavaVMBundlerVersionsDirURL;
    CFURLRef    TargetJavaVM;
    UInt8 pathToTargetJVM [PATH_MAX] = "\0";
    struct stat sbuf;


                // Look for the JavaVM bundle using its identifier
                JavaVMBundle = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.JavaVM") );

                if (JavaVMBundle != NULL) {
                        // Get a path for the JavaVM bundle
                        JavaVMBundleURL = CFBundleCopyBundleURL(JavaVMBundle);
                        CFRelease(JavaVMBundle);

                        if (JavaVMBundleURL != NULL) {
                                // Append to the path the Versions Component
                                JavaVMBundlerVersionsDirURL = CFURLCreateCopyAppendingPathComponent(kCFAllocatorDefault,JavaVMBundleURL,CFSTR("Versions"),true);
                                CFRelease(JavaVMBundleURL);

                                if (JavaVMBundlerVersionsDirURL != NULL) {
                                        // Append to the path the target JVM's Version
                                        TargetJavaVM = CFURLCreateCopyAppendingPathComponent(kCFAllocatorDefault,JavaVMBundlerVersionsDirURL,targetJVM,true);
                                        CFRelease(JavaVMBundlerVersionsDirURL);

                                        if (TargetJavaVM != NULL) {
                                                if (CFURLGetFileSystemRepresentation (TargetJavaVM,true,pathToTargetJVM,PATH_MAX )) {
                                                        // Check to see if the directory, or a sym link for the target JVM directory exists, and if so set the
                                                        // environment variable JAVA_JVM_VERSION to the target JVM.
                                                        if (stat((char*)pathToTargetJVM,&sbuf) == 0) {
                                                                // Ok, the directory exists, so now we need to set the environment var JAVA_JVM_VERSION to the CFSTR targetJVM
                                                                // We can reuse the pathToTargetJVM buffer to set the environement var.
                                                                if (CFStringGetCString(targetJVM,(char*)pathToTargetJVM,PATH_MAX,kCFStringEncodingUTF8))
                                                                        setenv("JAVA_JVM_VERSION", (char*)pathToTargetJVM,1);
                                                        }
                                                }
                                        CFRelease(TargetJavaVM);
                                        }
                                }
                        }
                }
        }

    /* JNI_VERSION_1_4 is used on Mac OS X to indicate the 1.4.x and later JVM's */
    args.version        = JNI_VERSION_1_4;


    args.options        = launchOptions->options;
    options                = launchOptions->options;
    args.nOptions = launchOptions->nOptions;

/*     if (access("/Users/cxh/ptII/ptolemy/plot/Plot.class", R_OK) == 0) { */
/*         ((JavaVMOption *)options)[args.nOptions].optionString = "-Djava.class.path=/Users/cxh/ptII"; */
/*     } else { */
/*         // Use ptsupport here in case we use SliderSource or some other actor */
/*         ((JavaVMOption *)options)[args.nOptions++].optionString = "-Djava.class.path=/Users/cxh/ptII/ptolemy/ptsupport.jar"; */
/*         //options[0].optionString = "-Djava.class.path=/Users/cxh/ptII/ptolemy/plot/plotapplication.jar"; */
/*     } */

    args.ignoreUnrecognized        = JNI_TRUE;


    /* start a VM session */
    result = JNI_CreateJavaVM(&theVM, (void**)&env, &args);

    if ( result != 0 ) {
        fprintf(stderr, "[JavaAppLauncher Error] Error starting up VM.\n");
        exit(result);
    }

#else // __MAC_OS_X_VERSION_10_0
args.version = JNI_VERSION_1_4;
args.nOptions = 1;
if (access("$path/ptolemy/plot/Plot.class", R_OK) == 0) {
    options[0].optionString = "-Djava.class.path=$path";
} else {
    // Use ptsupport here in case we use SliderSource or some other actor
    options[0].optionString = "-Djava.class.path=$path/ptolemy/ptsupport.jar";
    //options[0].optionString = "-Djava.class.path=$path/ptolemy/plot/plotapplication.jar";
}
args.options = options;
args.ignoreUnrecognized = JNI_FALSE;

JNI_CreateJavaVM(&jvm, (void **)&env, &args);
#endif // __MAC_OS_X_VERSION_10_0
#endif // _JAVA_INVOCATION_INTERFACE_INIT
/**/

/***initBlock***/
$actorSymbol(plotClass) = (*env)->FindClass(env, "ptolemy/plot/Plot");
if ($actorSymbol(plotClass) == 0x0) {
    fprintf(stderr, "Could not find class ptolemy/plot/Plot\n");
}

$actorSymbol(plotConstructor) = (*env)->GetMethodID
        (env, $actorSymbol(plotClass), "<init>", "()V");
$actorSymbol(plotObject) = (*env)->NewObject
        (env, $actorSymbol(plotClass),
        $actorSymbol(plotConstructor));
$actorSymbol(plotAddPoint) = (*env)->GetMethodID
        (env, $actorSymbol(plotClass), "addPoint", "(IDDZ)V");
$actorSymbol(plotMLApplicationClass) = (*env)->FindClass
        (env, "ptolemy/plot/plotml/PlotMLApplication");
$actorSymbol(plotMLApplicationConstructor) = (*env)->GetMethodID
        (env, $actorSymbol(plotMLApplicationClass), "<init>",
        "(Lptolemy/plot/PlotBox;[Ljava/lang/String;)V");
$actorSymbol(plotMLApplicationObject) = (*env)->NewObject
        (env, $actorSymbol(plotMLApplicationClass),
        $actorSymbol(plotMLApplicationConstructor),
        $actorSymbol(plotObject),
/* The following is a String array containing one empty String.
   If we use a NULL instead here, then a sample plot will be drawn.
   We don't want that.
 */
(*env)->NewObjectArray(env, 1, (*env)->FindClass(env, "java/lang/String"),
(*env)->NewStringUTF(env, "")));
/**/

/***configureBlock($text)***/
$actorSymbol(plotMLParserClass) = (*env)->FindClass
        (env, "ptolemy/plot/plotml/PlotMLParser");
$actorSymbol(plotMLParserConstructor) = (*env)->GetMethodID
        (env, $actorSymbol(plotMLParserClass), "<init>",
        "(Lptolemy/plot/Plot;)V");
$actorSymbol(plotMLParserObject) = (*env)->NewObject
        (env, $actorSymbol(plotMLParserClass),
        $actorSymbol(plotMLParserConstructor),
        $actorSymbol(plotObject));
$actorSymbol(plotMLParserParse) = (*env)->GetMethodID
        (env, $actorSymbol(plotMLParserClass), "parse",
       "(Ljava/net/URL;Ljava/lang/String;)V");
(*env)->CallVoidMethod(env, $actorSymbol(plotMLParserObject),
        $actorSymbol(plotMLParserParse), NULL,
        (*env)->NewStringUTF(env,
                $text));
/**/
