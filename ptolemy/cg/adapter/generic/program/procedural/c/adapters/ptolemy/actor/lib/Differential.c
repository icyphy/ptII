/***preinitBlock($type)***/
static $cgType(output) $actorSymbol(previous) = $zero_$cgType(output)();
$cgType(input) $actorSymbol(inputTemp);
/**/

/***fireBlock***/
$put(output, ($cgType(output))$subtract_$cgType(input)_$cgType(input)($actorSymbol(inputTemp), $actorSymbol(previous)));
$actorSymbol(inputTemp) = $get(input);
previous = $actorSymbol(inputTemp);
/**/
