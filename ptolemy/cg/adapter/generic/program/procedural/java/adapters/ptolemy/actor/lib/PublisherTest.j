// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if \$channel
// number is equal zero (the first channel). If so, then we increment.

/*** preinitBlock ***/
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
$actorSymbol(toleranceToken) = $new(Double($param(tolerance)));
/**/

/***fireBlock($channel)***/
$put(output#$channel, $get(input#$channel));
/**/

/***IntegerBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* IB $actorSymbol(), IntegerBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $param(tolerance)));
}
/**/


/***IntegerBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* IBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())
        > $param(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (Integer)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)));
}
/**/


/***DoubleBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $param(tolerance)));
}
/**/

/***DoubleBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* DBMC $channel of $actorSymbol() */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
   $actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
   if (Math.abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())

           /*- Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input))*/
               > $param(tolerance)) {
        throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (Integer)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)));
    /*Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input)));*/
   }
}
/**/

/***BooleanBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$param(correctValues, $actorSymbol(numberOfTokensSeen))
                && $actorSymbol(inputToken)))
        || ($param(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$actorSymbol(inputToken)) ) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($param(correctValues, $actorSymbol(numberOfTokensSeen)))));
}
/**/

/***BooleanBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (((!Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
      && $actorSymbol(inputToken)))
    || (Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
        && !$actorSymbol(inputToken)))) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean)));
}
/**/

/***StringBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$param(correctValues, $actorSymbol(numberOfTokensSeen))
                    .equals($actorSymbol(inputToken))) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen))));
}
/**/

/***StringBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$actorSymbol(inputToken).equals(
                    (String)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload))) {
    throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
            + $actorSymbol(numberOfTokensSeen)
            + ".\n Value was a String: \""
            + $actorSymbol(inputToken)
            + "\". Should have been a String: \""
            + (String)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)
            + "\"");
}
/**/

/***TokenBlock($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array
     && equals_Token_Token($actorSymbol(inputToken), Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))))
    || ($type(input) == TYPE_Array
        && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), 0), $actorSymbol(toleranceToken)))) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration "
                                 + $actorSymbol(numberOfTokensSeen)
                             + ".\n Value was:"
                             + $actorSymbol(inputToken)
                             + "Should have been within " + $param(tolerance) + " of: "
                             + Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))
                             + ".\n"));
}
/**/

/***TokenBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $get(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel))).payload.Boolean) {
    throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
            + $actorSymbol(numberOfTokensSeen)
            + ".\n Value was a String: \""
            + $actorSymbol(inputToken)
            + "\". Should have been a String: \""
            + (String)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)
            + "\"");
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    throw new RuntimeException(String.format("\nTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues)));
}
/**/
