/***preinitBlock($type)***/
static $targetType(output) $actorSymbol(previous) = $zero_$cgType(output)();
$targetType(input) $actorSymbol(inputTemp);
/**/

/***fireBlock***/
$put(output, ($targetType(output))$subtract_$cgType(input)_$cgType(input)($actorSymbol(inputTemp), $actorSymbol(previous)));
$actorSymbol(inputTemp) = $get(input);
previous = $actorSymbol(inputTemp);
/**/
