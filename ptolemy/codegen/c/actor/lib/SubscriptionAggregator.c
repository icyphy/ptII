/***preinitBlock($type)***/
$type $actorSymbol(result);
/**/

/*** fireBlock0 ***/
$actorSymbol(result) = $ref(($cgType(output)) input#0);
/**/

/*** fireBlock($channel) ***/
// FIXME: Handle multiplication
$actorSymbol(result) += $ref(($cgType(output)) input#$channel);
/**/

/*** fireBlock2 ***/
$ref(output) = $actorSymbol(result);
/**/
