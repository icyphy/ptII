/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initSum($zero)***/
$actorSymbol(result) = $zero;
/**/

/***minusOnlyInitSum($minusType)***/
$actorSymbol(result) = $zero_$minusType();
/**/
//$actorSymbol(result) = $negate_$minusType($get(minus#0));
//$zero_$minusType($get(minus#0));

/***plusBlock($channel, $type1, $type2)***/
if ($hasToken(plus#$channel)) {
    triggered = true;
    $actorSymbol(result) = $add_$type1_$type2($actorSymbol(result), $get(plus#$channel));
}
/**/

/***minusBlock($channel, $type1, $type2)***/
if ($hasToken(minus#$channel)) {
    triggered = true;
    $actorSymbol(result) = $subtract_$type1_$type2($actorSymbol(result), $get(minus#$channel));
}
/**/

/***outputBlock***/
if (triggered) {
    $put(output, $actorSymbol(result));
}
/**/
