/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initSumOld($type1, $type2)***/
$actorSymbol(result) = $zero_$cgType(output)OneArg($get(plus#0));
/**/

/***initSum($zero) ***/
$actorSymbol(result) = $zero;
/**/

/***minusOnlyInitSum($minusType)***/
//$actorSymbol(result) = $negate_$minusType($get(minus#0));
$actorSymbol(result) = $zero_$minusTypeOneArg($get(minus#0));
/**/

/***plusBlock($channel, $type1, $type2)***/
if ($hasToken(plus#$channel)) {
    $actorSymbol(result) = $add_$type1_$type2($actorSymbol(result), $get(plus#$channel));
}
/**/

/***minusBlock($channel, $type1, $type2)***/
if ($hasToken(minus#$channel)) {
    $actorSymbol(result) = $subtract_$type1_$type2($actorSymbol(result), $get(minus#$channel));
}
/**/

/***outputBlock***/
$put(output, $actorSymbol(result));
/**/
