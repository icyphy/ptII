/***preinitBlock***/
static $targetType(output) $actorSymbol(state);
/**/

/***initBlock***/
$actorSymbol(state) = $convert_$cgType(init)_$cgType(output)($val(init));
/**/

/***getTriggerTokens($channel)***/
$get(trigger, $channel)
/**/

/***fireBlock***/
$ref(output) = $actorSymbol(state);

// FIXME: this should be put into the postfireBlock
// but postfire code generation is not working correctly yet.
$actorSymbol(state) = $add_$cgType(output)_$cgType(step)($actorSymbol(state), $ref(step));
/**/

