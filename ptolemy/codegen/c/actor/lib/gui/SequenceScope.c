/***preinitBlock***/
    jdouble $actorSymbol(xValue);
/**/

/***initBlock***/     
    $actorSymbol(plotClass) = (*env)->FindClass(env, "ptolemy/plot/Plot");
    $actorSymbol(plotConstructor) = (*env)->GetMethodID
            (env, $actorSymbol(plotClass), "<init>", "()V");
    $actorSymbol(plotObject) = (*env)->NewObject
            (env, $actorSymbol(plotClass), $actorSymbol(plotConstructor));
    $actorSymbol(plotAddPoint) = (*env)->GetMethodID
            (env, $actorSymbol(plotClass), "addPoint", "(IDDZ)V");
            
    $actorSymbol(plotSetPointsPersistence) = (*env)->GetMethodID
            (env, $actorSymbol(plotClass), "setPointsPersistence", "(I)V");
    (*env)->CallVoidMethod(env, $actorSymbol(plotObject), 
            $actorSymbol(plotSetPointsPersistence), $val(persistence));  
            
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
    $actorSymbol(xValue) = $val(xInit);  
/**/

/***configureBlock($text)***/
    $actorSymbol(plotMLParserClass) = (*env)->FindClass
            (env, "ptolemy/plot/plotml/PlotMLParser");
    $actorSymbol(plotMLParserConstructor) = (*env)->GetMethodID
            (env, $actorSymbol(plotMLParserClass), "<init>", "(Lptolemy/plot/Plot;)V");
    $actorSymbol(plotMLParserObject) = (*env)->NewObject
            (env, $actorSymbol(plotMLParserClass), 
            $actorSymbol(plotMLParserConstructor), $actorSymbol(plotObject));
    $actorSymbol(plotMLParserParse) = (*env)->GetMethodID
            (env, $actorSymbol(plotMLParserClass), "parse", 
            "(Ljava/net/URL;Ljava/lang/String;)V");
    (*env)->CallVoidMethod(env, $actorSymbol(plotMLParserObject),         
            $actorSymbol(plotMLParserParse), NULL, 
            (*env)->NewStringUTF(env, $text));
/**/

/***plotBlock($channel)***/
    (*env)->CallVoidMethod(env, $actorSymbol(plotObject), $actorSymbol(plotAddPoint), 
            $channel + $val(startingDataset), $actorSymbol(xValue), 
            $ref(input#$channel), JNI_TRUE);           
/**/

/***updateBlock***/   
    $actorSymbol(xValue) += $val(xUnit);         
/**/

