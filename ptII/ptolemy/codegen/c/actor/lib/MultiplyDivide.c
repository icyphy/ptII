/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initProduct($multiplyType, $outputType)***/
$actorSymbol(result) = $convert_$multiplyType_$outputType($ref(multiply#0));
/**/

/***divideOnlyInitProduct($divideType)***/
$actorSymbol(result) = $divide_one_$divideType($ref(divide#0));
/**/

/***multiplyBlock($channel, $outputType, $multiplyType)***/
$actorSymbol(result) = $multiply_$outputType_$multiplyType($actorSymbol(result), $ref(multiply#$channel));
/**/

/***divideBlock($channel, $outputType, $divideType)***/
$actorSymbol(result) = $divide_$outputType_$divideType($actorSymbol(result), $ref(divide#$channel));
/**/

/***outputBlock***/
$ref(output) = $actorSymbol(result);
/**/
