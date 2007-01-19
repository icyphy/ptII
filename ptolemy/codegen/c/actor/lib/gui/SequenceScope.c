/***preinitBlock***/
jdouble $actorSymbol(xValue);
jmethodID $actorSymbol(plotSetPointsPersistence);
/**/

/***initBlock***/     
$actorSymbol(plotSetPointsPersistence) = (*env)->GetMethodID
        (env, $actorSymbol(plotClass), "setPointsPersistence", "(I)V");
(*env)->CallVoidMethod(env, $actorSymbol(plotObject), 
        $actorSymbol(plotSetPointsPersistence), $val(persistence));             
$actorSymbol(xValue) = $val(xInit);  
/**/
