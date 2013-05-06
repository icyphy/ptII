/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initProduct($type1, $type2)***/
if ($hasToken(multiply#0)) {
	$actorSymbol(result) = $convert_$type1_$type2($get(multiply#0));
}
else {
	$actorSymbol(result) = $convert_$type1_Double(1.0);
}
/**/

/***divideOnlyInitProduct($divideType)***/
if ($hasToken(divide#0)) {
	$actorSymbol(result) = $divide_one_$divideType($get(divide#0));
}
else {
	$actorSymbol(result) = $divide_one_Double(1.0);
}
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
