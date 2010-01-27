/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/***initSum()***/
$actorSymbol(result) = $zero_$cgType(output)();
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
