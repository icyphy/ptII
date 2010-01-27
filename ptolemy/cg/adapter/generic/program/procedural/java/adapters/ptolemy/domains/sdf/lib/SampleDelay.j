/***preinitBlock($type)***/
$type $actorSymbol(lastVal);
/**/

/***fireBlock***/
$put(output, $actorSymbol(lastVal));
$actorSymbol(lastVal) = $get(input);
/**/
