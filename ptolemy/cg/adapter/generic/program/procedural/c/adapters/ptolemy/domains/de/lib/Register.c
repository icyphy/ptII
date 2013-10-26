/***preinitBlock***/
/** The value that is output when no input has yet been received.
 *  If this is changed during execution, then the output will match
 *  the new value until another input is received.
 *  The type should be the same as the input port.
 *  @see #typeConstraints()
 */
Token $actorSymbol(initialValue);

/** The recorded inputs last seen. */
Token * $actorSymbol(lastInputs);
int $actorSymbol(size_lastInputs);
/**/

/***absentInitBlock***/
// Initialization of the parameters
$actorSymbol(lastInputs) = NULL;
$actorSymbol(size_lastInputs) = 0;
/**/

/***initBlock($inputSize, $initialValueType, $initialValueToken)***/
$actorSymbol(lastInputs) = calloc($inputSize, sizeof(Token));
$actorSymbol(size_lastInputs) = $inputSize;

int i;
for (i = 0; i < $inputSize; i++) {
        $actorSymbol(lastInputs)[i].type = TYPE_$initialValueType;
        $actorSymbol(lastInputs)[i].payload.$initialValueType = $initialValueToken;
}
/**/

/***beginPreFireBlock***/
// If the trigger input is not connected, never fire.
boolean hasToken = false;
/**/

/***inputConnectedPreFireBlock***/
hasToken = $hasToken(input#0);
/**/

/***preFireLoopBlock($channel)***/
hasToken |= $hasToken(trigger#$channel);
/**/

/***endPreFireBlock***/
return hasToken;
/**/


/***InitFireBlock($inputWidth, $outputWidth, $triggerWidth)***/
int inputWidth = $inputWidth;
// If we have a trigger...
boolean triggered = false;
int i = 0;

// If the <i>initialValue</i> parameter was not set, or if the
// width of the input has changed.
if ($actorSymbol(lastInputs) == NULL) {
        $actorSymbol(lastInputs) = calloc(inputWidth, sizeof(Token));
        $actorSymbol(size_lastInputs) = inputWidth;
        for (i = 0; i < inputWidth; i++) {
                $actorSymbol(lastInputs)[i].type = -2;
        }
}
else if ($actorSymbol(size_lastInputs) != inputWidth) {
        free($actorSymbol(lastInputs));
        $actorSymbol(lastInputs) = calloc(inputWidth, sizeof(Token));
        $actorSymbol(size_lastInputs) = inputWidth;
        for (i = 0; i < inputWidth; i++) {
                $actorSymbol(lastInputs)[i].type = -2;
        }
}

/**/

// Consume the inputs we save.
/***inputChannelLoopFireBlock($channel)***/
while ($hasToken(input#$channel)) {
        $actorSymbol(lastInputs)[$channel].type = TYPE_$cgType(input);
        $actorSymbol(lastInputs)[$channel].payload.$cgType(input) = $get(input#$channel);
}

/**/

// Consume the inputs we don't save.
/***throwTokensLoopFireBlock($channel)***/
while ($hasToken(input#$channel)) {
        (void)$get(input#$channel);
}
/**/

/***triggerLoopFireBlock($channel)***/
if ($hasToken(trigger#$channel)) {
        // Consume the trigger token.
        (void)$get(trigger#$channel);
        triggered = true;
}
/**/

/***ifTriggeredFireBlock***/
if (triggered) {
/**/

        /***ifTriggeredLoopFireBlock($channel)***/
        // Do not output anything if the <i>initialValue</i>
        // parameter was not set and this actor has not
        // received any inputs.
        if ($actorSymbol(lastInputs)[$channel].type != -2) {
                // Output the most recent token, assuming the
                // receiver has a FIFO behavior.
                $put(output#$channel, $actorSymbol(lastInputs)[$channel].payload.$cgType(output));
        }
        /**/

/***endIfTriggeredFireBlock***/
}
/**/

/***wrapupBlock***/
if ($actorSymbol(lastInputs) != NULL)
        free($actorSymbol(lastInputs));
/**/
