/*** preinitBlock ***/
// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if \$channel
// number is equal zero (the first channel). If so, then we increment.
int $actorSymbol(numberOfTokensSeen) = -1;
$targetType(input) $actorSymbol(inputToken);
/**/

/*** TokenPreinitBlock($channel)***/
Token $actorSymbol(correctValuesThisFiring_$channel);
/**/

/*** toleranceTokenPreinitBlock***/
static Token $actorSymbol(toleranceToken);
/**/

/*** toleranceTokenInitBlock***/
$actorSymbol(toleranceToken) = $new(Double($ref(tolerance)));
/**/

/***IntegerBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), IntegerBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    System.exit(-1);
}
/**/


/***IntegerBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
	$actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));

if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())
        > $ref(tolerance)) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Integer);
    System.exit(-1);
}
/**/


/***DoubleBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    System.exit(-1);
}
/**/

/***DoubleBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
	$actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input))
        > $ref(tolerance)) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input));
    System.exit(-1);
}
/**/

/***BooleanBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && $actorSymbol(inputToken)))
        || ($ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$actorSymbol(inputToken)) ) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($ref(correctValues, $actorSymbol(numberOfTokensSeen))));
    System.exit(-1);
}
/**/

/***BooleanBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
	$actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (((!Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
      && $actorSymbol(inputToken)))
    || (Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
        && !$actorSymbol(inputToken)))) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean));
    System.exit(-1);
}
/**/

/***StringBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp($ref(correctValues, $actorSymbol(numberOfTokensSeen)),
                    $actorSymbol(inputToken)) != 0) ) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)));
    System.exit(-1);
}
/**/

/***StringBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
	$actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp((char *)$actorSymbol(inputToken),
                    Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.String)
                != 0)) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.String);
    System.exit(-1);
}
/**/

/***TokenBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array
            && equals_Token_Token($actorSymbol(inputToken), $actorSymbol(numberOfTokensSeen)))
        || ($type(input) == TYPE_Array
	    && !isCloseTo_Token_Token($actorSymbol(inputToken), Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen)), $actorSymbol(toleranceToken)))) {
    System.out.print("\nTest $actorSymbol($channel) fails in iteration "
    			     + $actorSymbol(numberOfTokensSeen)
			     + ".\n Value was:"
			     + $actorSymbol(inputToken)
			     + "Should have been within %10.30g of: "
			     + Array_get($ref(correctValues, $actorSymbol(numberOfTokensSeen))) 
			     + ".\n");
    System.exit(-1);
}
/**/

/***TokenBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
	$actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel))).payload.Boolean) {
    System.out.printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($actorSymbol(inputToken)::toString()).payload.String,
            $tokenFunc(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)::toString()).payload.String);
    System.exit(-1);
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    System.out.printf("\nTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
    System.exit(-2);
}
/**/
