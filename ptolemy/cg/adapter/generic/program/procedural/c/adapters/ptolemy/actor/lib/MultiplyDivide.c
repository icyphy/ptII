/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initProduct($multiplyType, $outputType)***/
if ($hasToken(multiply#0)) {
    $actorSymbol(result) = $convert_$multiplyType_$outputType($get(multiply#0));
}
/**/

/***divideOnlyInitProduct($divideType)***/
if ($hasToken(divide#0)) {
    $actorSymbol(result) = $divide_one_$divideType($get(divide#0));
}
/**/

/***multiplyBlock($channel, $outputType, $multiplyType)***/
if ($hasToken(multiply#channel)) {
    $actorSymbol(result) = $multiply_$outputType_$multiplyType($actorSymbol(result), $get(multiply#$channel));
}
/**/

/***divideBlock($channel, $outputType, $divideType)***/
if ($hasToken(divide#channel)) {
    $actorSymbol(result) = $divide_$outputType_$divideType($actorSymbol(result), $get(divide#$channel));
}
/**/

/***outputBlock***/
$put(output, $actorSymbol(result));
/**/
