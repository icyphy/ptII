/***preinitBlock_hasInitialValue($channel)***/
static $targetType(output) $actorSymbol(storedValue_$channel) = ($targetType(output)) $val(initialValue);
static unsigned int $actorSymbol(storedValue_$channel_hasValue) = 1;
/**/

/***preinitBlock_noInitialValue($channel)***/
static $targetType(output) $actorSymbol(storedValue_$channel);
static unsigned int $actorSymbol(storedValue_$channel_hasValue) = 0;
/**/

/***triggerBlock($channel)***/
if ($hasToken(trigger) && $actorSymbol(storedValue_$channel_hasValue)) {
        $put(output#$channel, $actorSymbol(storedValue_$channel))
}
/**/

/***updateValueBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(storedValue_$channel) = ($targetType(output)) $get(input#$channel);
        $actorSymbol(storedValue_$channel_hasValue) = 1;
}
/**/
