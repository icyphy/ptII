/***preinitBlock_hasInitialValue($channel)***/
static $cgType(output) $actorSymbol(storedValue_$channel) = ($cgType(output)) $val(initialValue);
static bool $actorSymbol(storedValue_$channel_hasValue) = true;
/**/

/***preinitBlock_noInitialValue($channel)***/
static $cgType(output) $actorSymbol(storedValue_$channel);
static bool $actorSymbol(storedValue_$channel_hasValue) = false;
/**/

/***triggerBlock($channel)***/
if(hasToken(trigger) && $actorSymbol(storedValue_$channel_hasValue)){
	$put(output#$channel, $actorSymbol(storedValue_$channel))
}
/**/

/***updateValueBlock($channel)***/
if(hasToken(input#$channel)){
	$actorSymbol(storedValue_$channel) = ($cgType(output))$get(input#$channel);
	$actorSymbol(storedValue_$channel_hasValue) = true;
}
/**/
