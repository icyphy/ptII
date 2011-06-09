/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/*** fireBlock0 ***/
$actorSymbol(result) = $ref(($cgType(output)) input#0);
/**/

/*** fireBlockAdd($channel) ***/
$actorSymbol(result) += $ref(($cgType(output)) input#$channel);
/**/

/*** fireBlockMultiply($channel) ***/
$actorSymbol(result) *= $ref(($cgType(output)) input#$channel);
/**/

/*** fireBlock2 ***/
$ref(output) = $actorSymbol(result);
/**/
