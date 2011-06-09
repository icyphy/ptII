/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initSum***/
$actorSymbol(result) = $ref(plus#0);
/**/

/***convertAndInitSum($type1, $type2)***/
$actorSymbol(result) = $convert_$type1_$type2($ref(plus#0));
/**/

/***minusOnlyInitSum($minusType)***/
$actorSymbol(result) = $negate_$minusType($ref(minus#0));
/**/

/***plusBlock($channel, $type1, $type2)***/
$actorSymbol(result) = $add_$type1_$type2($actorSymbol(result), $ref(plus#$channel));
/**/

/***minusBlock($channel, $type1, $type2)***/
$actorSymbol(result) = $subtract_$type1_$type2($actorSymbol(result), $ref(minus#$channel));
/**/

/***outputBlock***/
$ref(output) = $actorSymbol(result);
/**/
