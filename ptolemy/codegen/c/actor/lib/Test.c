/*** preinitBlock ***/
// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if \$channel
// number is equal zero (the first channel). If so, then we increment.
int $actorSymbol(numberOfTokensSeen) = -1;
$targetType(input) $actorSymbol(inputToken);
Token $actorSymbol(trainedValues);
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

/***IntBlock($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), IntBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($actorSymbol(inputToken)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    exit(-1);
}
/**/


/***IntBlockMultiChannel($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));

if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($actorSymbol(inputToken)
                - IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $ref(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
            IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/


/***DoubleBlock($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($actorSymbol(inputToken)
                - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $ref(tolerance),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $ref(tolerance));
    exit(-1);
}
/**/

/***DoubleBlockMultiChannel($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && fabs($actorSymbol(inputToken)
                - DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $ref(tolerance)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(tolerance),
            DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/

/***BooleanBlock($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && $actorSymbol(inputToken)))
        || ($ref(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$actorSymbol(inputToken)) ) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($ref(correctValues, $actorSymbol(numberOfTokensSeen))));
    exit(-1);
}
/**/

/***BooleanBlockMultiChannel($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (((!BooleanArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel)
      && $actorSymbol(inputToken)))
    || (BooleanArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel)
        && !$actorSymbol(inputToken)))) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString(BooleanArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel)));
    exit(-1);
}
/**/

/***StringBlock($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp($ref(correctValues, $actorSymbol(numberOfTokensSeen)),
                    $actorSymbol(inputToken)) != 0) ) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $ref(correctValues, $actorSymbol(numberOfTokensSeen)));
    exit(-1);
}
/**/

/***StringBlockMultiChannel($channel)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
$ref(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (strcmp((char *)$actorSymbol(inputToken),
                        StringArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
                != 0)) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            StringArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/

/***ArrayBlock($channel, $inputType)***/
$get(input, $channel)
// Array
$actorSymbol(numberOfTokensSeen)++;
$actorSymbol(inputToken) = $ref(input#$channel);

/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
        $actorSymbol(trainedValues) = Array_get($ref(correctValues, $actorSymbol(numberOfTokensSeen)), $channel);

        if (!$typeFunc(
                TYPE_$inputType::isCloseTo(
                                $actorSymbol(trainedValues),
                                $actorSymbol(inputToken),
                                $actorSymbol(toleranceToken))).payload.Boolean) {

                printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %s. Should have been within %10.30g of: %s.\n",
                                $actorSymbol(numberOfTokensSeen),
                                $tokenFunc($actorSymbol(inputToken)::toString()).payload.String,
                                $ref(tolerance),
                                $typeFunc(TYPE_$inputType::toString($actorSymbol(trainedValues))).payload.String);
                exit(-1);
        }
}
/**/

/***TokenBlock($channel, $inputType)***/
        $get(input, $channel)

        //Token
        $actorSymbol(numberOfTokensSeen)++;
        $actorSymbol(inputToken) = $ref(input#$channel);

        /* If the type of the input is an array, then cast the input to
         * the type of the elements of the elements of correctValues. */
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
                $actorSymbol(trainedValues) = $ref(correctValues, $actorSymbol(numberOfTokensSeen));

                if (!$typeFunc(
                        TYPE_$inputType::isCloseTo(
                                        $actorSymbol(trainedValues),
                                        $actorSymbol(inputToken),
                                        $actorSymbol(toleranceToken))).payload.Boolean) {

                        printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %s. Should have been within %10.30g of: %s.\n",
                                        $actorSymbol(numberOfTokensSeen),
                                        $tokenFunc($actorSymbol(inputToken)::toString()).payload.String,
                                                                                                                $ref(tolerance),
                                        $typeFunc(TYPE_$inputType::toString($actorSymbol(trainedValues))).payload.String);
                        exit(-1);
            }
        }
/**/


/***TokenBlockMultiChannel($channel, $inputType)***/
$get(input, $channel)

$actorSymbol(inputToken) = $ref(input#$channel);
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) =
Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && !$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel))).payload.Boolean) {
    printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($actorSymbol(inputToken)::toString()).payload.String,
            $tokenFunc(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)::toString()).payload.String);
    exit(-1);
}
/**/

/***postfireBlock***/
// FIXME: should we update this here?
//$actorSymbol(numberOfTokensSeen)++;
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    printf("\nTest produced only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
    exit(-2);
}
/**/
