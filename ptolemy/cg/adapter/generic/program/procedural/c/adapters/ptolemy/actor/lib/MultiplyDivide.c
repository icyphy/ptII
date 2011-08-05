/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initProduct($one, $cgType)***/
$actorSymbol(result) = $one_$cgType(output)();
/**/

/***multiplyBlock($channel, $outputType, $multiplyType)***/
if ($hasToken(multiply#$channel)) {
    $actorSymbol(result) = $multiply_$outputType_$multiplyType($actorSymbol(result), $get(multiply#$channel));
}
/**/

/***divideBlock($channel, $outputType, $divideType)***/
if ($hasToken(divide#$channel)) {
    $actorSymbol(result) = $divide_$outputType_$divideType($actorSymbol(result), $get(divide#$channel));
}
/**/

/***outputBlock***/
$put(output, $actorSymbol(result));
/**/
