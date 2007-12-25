/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initProduct($type1, $type2)***/
$actorSymbol(result) = $convert_$type1_$type2($ref(multiply#0));
/**/

/***divideOnlyInitProduct($divideType)***/
$actorSymbol(result) = $divideType_zero($ref(divide#0));
/**/

/***multiplyBlock($channel, $type1, $type2)***/
$actorSymbol(result) = $multiply_$type1_$type2($actorSymbol(result), $ref(multiply#$channel));
/**/

/***divideBlock($channel, $type1, $type2)***/
$actorSymbol(result) = $divide_$type1_$type2($actorSymbol(result), $ref(divide#$channel));
/**/

/***outputBlock***/
$ref(output) = $actorSymbol(result);
/**/
