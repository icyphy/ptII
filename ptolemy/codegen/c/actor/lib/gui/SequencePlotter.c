/***preinitBlock***/
jdouble $actorSymbol(xValue);
/**/

/***initBlock***/           
$actorSymbol(xValue) = $val(xInit);  
/**/

/***plotBlock($channel)***/
(*env)->CallVoidMethod(env, $actorSymbol(plotObject), $actorSymbol(plotAddPoint), 
        $channel + $val(startingDataset), $actorSymbol(xValue), 
        $ref(input#$channel), JNI_TRUE);           
/**/

/***updateBlock***/   
$actorSymbol(xValue) += $val(xUnit);         
/**/

