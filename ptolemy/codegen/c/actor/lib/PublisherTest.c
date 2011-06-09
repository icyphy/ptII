/*** preinitBlock ***/
// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if channel
// number is equal zero (the first channel). If so, then we increment.
int $actorSymbol(numberOfTokensSeen) = -1;
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

/***fireBlock($channel)***/
$ref(output#$channel) = $ref(($cgType(output)) input#$channel);
/**/

/***IntBlock($channel)***/
// IntBlock($channel)
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), IntBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($ref(input#$channel)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %f and %f\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    exit(-1);
}
/**/


/***IntBlockMultiChannel($channel)***/
if ($channel == 0) {
    $actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));

if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($ref(input#$channel)
                - IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $ref(tolerance)) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %f of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            $ref(tolerance),
            IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/


/***DoubleBlock($channel)***/
//DoubleBlock($channel)
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($ref(input#$channel)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been between: %f and %f\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    exit(-1);
}
/**/

/***DoubleBlockMultiChannel($channel)***/
if ($channel == 0) {
    $actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($ref(input#$channel)
                - DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $ref(tolerance)) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was: %g. Should have been within %f of: %g\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            $ref(tolerance),
            DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/

/***BooleanBlock($channel)***/
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && $ref(input#$channel)))
        || ($ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$ref(input#$channel)) ) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($ref(input#$channel)),
            BooleantoString($ref(correctValues, $actorSymbol(numberOfTokensSeen))));
    exit(-1);
}
/**/

/***BooleanBlockMultiChannel($channel)***/
if ($channel == 0) {
    $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (((!Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
      && $ref(input#$channel)))
    || (Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean
        && !$ref(input#$channel)))) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($ref(input#$channel)),
            BooleantoString(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.Boolean));
    exit(-1);
}
/**/

/***StringBlock($channel)***/
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp($ref(correctValues, $actorSymbol(numberOfTokensSeen)),
                    $ref(input#$channel)) != 0) ) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)));
    exit(-1);
}
/**/

/***StringBlockMultiChannel($channel)***/
if ($channel == 0) {
    $actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp((char *)$ref(input#$channel),
                    Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.String)
                != 0)) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $ref(input#$channel),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).payload.String);
    exit(-1);
}
/**/

/***TokenBlock($channel)***/
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array
            && !$tokenFunc($ref(input#$channel)::equals($ref(correctValues, $actorSymbol(numberOfTokensSeen)))).payload.Boolean)
        || ($type(input) == TYPE_Array
                && !$tokenFunc($typeFunc(TYPE_Array::convert($ref(input#$channel), Array_get(Array_get($ref(correctValues, $actorSymbol(numberOfTokensSeen)), 0), 0).type))::isCloseTo(Array_get($ref(correctValues, $actorSymbol(numberOfTokensSeen)), 0), $actorSymbol(toleranceToken))).payload.Boolean)) {

    printf("\nPublisherTest $actorSymbol($channel) fails in interation %d.\n Value was: %s. Should have been within %f of: %s.\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($ref(input#$channel)::toString()).payload.String,
                                                $ref(tolerance),
            $tokenFunc($ref(correctValues, $actorSymbol(numberOfTokensSeen))::toString()).payload.String);
    exit(-1);
}
/**/

/***TokenBlockMultiChannel($channel)***/
if ($channel == 0) {
    $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$tokenFunc($ref(input#$channel)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel))).payload.Boolean) {
    printf("\nPublisherTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($ref(input#$channel)::toString()).payload.String,
            $tokenFunc(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)::toString()).payload.String);
    exit(-1);
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    printf("\nPublisherTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
    exit(-2);
}
/**/
