/***plotBlock($channel)***/
(*env)->CallVoidMethod(env, $actorSymbol(plotObject), $actorSymbol(plotAddPoint),
        $channel + $val(startingDataset), $ref(inputX#$channel),
        $ref(inputY#$channel), JNI_TRUE);
/**/

