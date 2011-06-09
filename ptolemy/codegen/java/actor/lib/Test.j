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

/* IB $actorSymbol(), IntegerBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance)));
}
/**/


/***IntegerBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* IBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
 $ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())
        > $ref(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
            (Integer)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)));
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
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance)));
}
/**/

/***DoubleBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* DBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && Math.abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)).$lcCgType(input)Value())

    /*- Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input))*/
        > $ref(tolerance)) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
             (Number)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload)));
    /*Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.$cgType(input)));*/
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
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($ref(correctValues, $actorSymbol(numberOfTokensSeen)))));
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
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean)));
}
/**/

/***StringBlock($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                    .equals($actorSymbol(inputToken))) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen))));
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
$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array
#ifdef PTCG_TYPE_Matrix
     && $type(input) != TYPE_Matrix
#endif
     && !equals_Token_Token($actorSymbol(inputToken), Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen))))
    || ($type(input) == TYPE_Array
        && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get(Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen)), 0), $actorSymbol(toleranceToken)))
#ifdef PTCG_TYPE_Matrix
    || ($type(input) == TYPE_Matrix
        && !$isCloseTo_Token_Token(Matrix_get($actorSymbol(inputToken), 0, 0), Matrix_get(Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen)), 0, 0), $actorSymbol(toleranceToken)))
#endif
    ) {
    throw new RuntimeException(String.format("\nTest $actorSymbol($channel) fails in iteration "
                                 + $actorSymbol(numberOfTokensSeen)
                             + ".\n Value was:"
                             + $actorSymbol(inputToken)
                             + "Should have been within " + $ref(tolerance) + " of: "
                             + Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen))
                             + ".\n"));
}
/**/

/***TokenBlockMultiChannel($channel)***/
$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* TBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
    //if ($type(input) != TYPE_Array) {
      if (!$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)))) {
//     throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
//             + $actorSymbol(numberOfTokensSeen)
//             + ".\n Value was: \""
//             + $actorSymbol(inputToken)
//             + "\". Should have been: \""
//             + Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)
//             + "\"");
//       }
//    } else {
    throw new RuntimeException("Test $actorSymbol($channel) fails in iteration "
            + $actorSymbol(numberOfTokensSeen)
            + ".\n Value was: '"
            + $tokenFunc($actorSymbol(inputToken)::toString()).payload
            + "'. Should have been: \""
            + Array_toString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)).payload
            + "\"");
      }
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    throw new RuntimeException(String.format("\nTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues)));
}
/**/
