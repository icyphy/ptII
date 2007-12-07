/***preinitBlock***/
#ifndef _JAVA_INVOCATION_INTERFACE_PREINIT
#define _JAVA_INVOCATION_INTERFACE_PREINIT
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
args.version = JNI_VERSION_1_4;
args.nOptions = 1;
options[0].optionString = "-Djava.class.path=$path";
args.options = options;
args.ignoreUnrecognized = JNI_FALSE;

JNI_CreateJavaVM(&jvm, (void **)&env, &args);
#endif
/**/

/***initBlock***/
$actorSymbol(plotClass) = (*env)->FindClass(env, "ptolemy/plot/Plot");
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


