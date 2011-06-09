/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/*** fireBlock0 ***/
$actorSymbol(result) = $convert_$cgType(input)_$cgType(output)($get(input));
/**/

/*** fireBlockAdd($channel) ***/
$actorSymbol(result) += $convert_$cgType(input)_$cgType(output)($get(input#$channel));
/**/

/*** fireBlockMultiply($channel) ***/
$actorSymbol(result) *= $convert_$cgType(input)_$cgType(output)($get(input#$channel));
/**/

/*** fireBlock2 ***/
$put(output, $actorSymbol(result));
/**/
