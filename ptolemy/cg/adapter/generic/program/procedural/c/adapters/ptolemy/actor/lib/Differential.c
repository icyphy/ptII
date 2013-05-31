/***preinitBlock()***/
$targetType(output) $actorSymbol(previous);
$targetType(input) $actorSymbol(inputTemp);
$targetType(output) $actorSymbol(differential);
/**/

/***initBlock()***/
$actorSymbol(previous) = $zero_$cgType(output)();
/**/

/***fireBlock***/
$actorSymbol(differential) = ($targetType(output))$subtract_$cgType(input)_$cgType(input)($actorSymbol(inputTemp), $actorSymbol(previous));
$put(output, $actorSymbol(differential));
$actorSymbol(inputTemp) = $get(input);
$actorSymbol(previous) = $actorSymbol(inputTemp);
/**/
