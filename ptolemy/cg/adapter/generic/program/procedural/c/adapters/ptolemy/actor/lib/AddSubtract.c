/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initSum($type1, $type2)***/
if ($hasToken(plus#0)) {
    $actorSymbol(result) = $convert_$type1_$type2($get(plus#0));
}
/**/

/***minusOnlyInitSum($minusType)***/
if ($hasToken(minus#0)) {
    $actorSymbol(result) = $negate_$minusType($get(minus#0));
}
/**/

/***plusBlock($channel, $type1, $type2)***/
if ($hasToken(plus#0)) {
    $actorSymbol(result) = $add_$type1_$type2($actorSymbol(result), $get(plus#$channel));
}
/**/

/***minusBlock($channel, $type1, $type2)***/
if ($hasToken(minus#0)) {
    $actorSymbol(result) = $subtract_$type1_$type2($actorSymbol(result), $get(minus#$channel));
}
/**/

/***outputBlock***/
$put(output, $actorSymbol(result));
/**/
