/***preinitBlock***/
/** The initial output value. If this is set, it specifies the
 *  first output value produced when the first input arrives.
 *  If it is not set, then no output is produced on the first
 *  firing.
 */
Token $actorSymbol(initialValue);

// Previous input.
Token $actorSymbol(previous);

// Temporary previous input.
Token $actorSymbol(tempPrevious);
/**/

/***absentInitBlock***/
// Initialization of the parameters
$actorSymbol(initialValue).type = -2;

// Note that this might be null, if it has not been set.
$actorSymbol(previous) = $actorSymbol(initialValue);
$actorSymbol(tempPrevious).type = -2;
/**/

/***initBlock($initialValueType, $initialValueToken)***/
// Initialization of the parameters
$actorSymbol(initialValue).type = TYPE_$initialValueType;
$actorSymbol(initialValue).payload.$initialValueType = $initialValueToken;

// Note that this might be null, if it has not been set.
$actorSymbol(previous) = $actorSymbol(initialValue);
$actorSymbol(tempPrevious).type = -2;
/**/

/***fireBlock***/
if ($actorSymbol(previous).type != -2) {
        $put(output, $actorSymbol(previous).payload.$cgType(output));
}

if ($hasToken(input)) {
        $actorSymbol(tempPrevious).type = TYPE_$cgType(input);
        $actorSymbol(tempPrevious).payload.$cgType(output) = $get(input);
}
/**/

/***postFireBlock***/
$actorSymbol(previous) = $actorSymbol(tempPrevious);
$actorSymbol(tempPrevious).type = -2;
/**/
