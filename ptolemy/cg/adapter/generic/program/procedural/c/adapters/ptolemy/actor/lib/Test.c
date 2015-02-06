/*** preinitBlock ***/
// Initialize to -1 because we ALWAYS increment first.
// This is more convenient for multiport, where we check if \$channel
// number is equal zero (the first channel). If so, then we increment.
int $actorSymbol(numberOfTokensSeen);
$targetType(input) $actorSymbol(inputToken);
/**/

/*** initBlock ***/
$actorSymbol(numberOfTokensSeen) = -1;
/**/

/*** TokenPreinitBlock($channel)***/
Token* $actorSymbol(correctValuesThisFiring_$channel);
/**/

/*** toleranceTokenPreinitBlock***/
static Token* $actorSymbol(toleranceToken);
/**/

/*** toleranceTokenInitBlock***/
$actorSymbol(toleranceToken) = $new(Double($param(tolerance)));
/**/

/***ComplexBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;

/* Complex $actorSymbol(), ComplexBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
   && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), $actorSymbol(toleranceToken))) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %s. Should have been within %g of %s.\n",
            $actorSymbol(numberOfTokensSeen),
            $ComplextoString($actorSymbol(inputToken)),
            $param(tolerance),
            $ComplextoString(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))));
   exit(-1);
}
/**/

/***ComplexBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* Complex $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)->payload)).$lcCgType(input)Value())
        > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (int)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)->payload));
   exit(-1);
}
/**/

/***IntBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;

/* IB $actorSymbol(), intBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
            $param(tolerance));
   exit(-1);
}
/**/


/***IntBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* IBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
        > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            IntArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
    exit(-1);
}
/**/


/***DoubleBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;

/* $actorSymbol(), DoubleBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
            $param(tolerance));
    exit(-1);
}
/**/

/***DoubleBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* DBMC $channel of $actorSymbol() */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
   $actorSymbol(correctValuesThisFiring_$channel) =
       $param(correctValues, $actorSymbol(numberOfTokensSeen));
   if (abs($actorSymbol(inputToken)
                - DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))
           > $param(tolerance)) {
       fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %10.30g. Should have been within %10.30g of: %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            DoubleArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
       exit(-1);
   }
}
/**/

/***BooleanBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;
if (($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && (!$param(correctValues, $actorSymbol(numberOfTokensSeen))
                && $actorSymbol(inputToken)))
        || ($param(correctValues, $actorSymbol(numberOfTokensSeen))
                && !$actorSymbol(inputToken)) ) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            BooleantoString($actorSymbol(inputToken)),
            BooleantoString($param(correctValues, $actorSymbol(numberOfTokensSeen))));
    exit(-1);
}
/**/

/***BooleanBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
    && (!Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).getPayload().equals(
       $actorSymbol(inputToken)))) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a token of value: %s. Should have been a token of value: %s\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel).getPayload());
   exit(-1);
}
/**/

/***StringBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && strcmp($actorSymbol(inputToken),$param(correctValues, $actorSymbol(numberOfTokensSeen)))) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)));
   exit(-1);
}
/**/

/***StringBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && strcmp($actorSymbol(inputToken), StringArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel))) {
        fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
                    $actorSymbol(numberOfTokensSeen),
                    $actorSymbol(inputToken),
                    StringArray_get($actorSymbol(correctValuesThisFiring_$channel), $channel));
   exit(-1);
}
/**/

/***TokenBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;
/* If the type of the input is an array, then cast the input to
 * the type of the elements of the elements of correctValues. */
if (($type(input) != TYPE_Array

#ifdef TYPE_BooleanArray
     && $type(input) != TYPE_BooleanArray
#endif

#ifdef TYPE_DoubleArray
     && $type(input) != TYPE_DoubleArray
#endif

#ifdef TYPE_IntArray
     && $type(input) != TYPE_IntArray
#endif

#ifdef TYPE_StringArray
     && $type(input) != TYPE_StringArray
#endif

#ifdef TYPE_Matrix
     && $type(input) != TYPE_Matrix
#endif

                && !equals_Token_Token($actorSymbol(inputToken), Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))))

     || (($type(input) == TYPE_Array
#ifdef TYPE_BooleanArray
          || $type(input) == TYPE_BooleanArray
#endif
#ifdef TYPE_DoubleArray
          || $type(input) == TYPE_DoubleArray
#endif
#ifdef TYPE_IntArray
          || $type(input) == TYPE_IntArray
#endif
#ifdef TYPE_StringArray
          || $type(input) == TYPE_StringArray
#endif
          )
             && !$isCloseTo_Token_Token($actorSymbol(inputToken), Array_get(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), 0), $actorSymbol(toleranceToken)))

#ifdef TYPE_Matrix
     || ($type(input) == TYPE_Matrix
             && !$isCloseTo_Token_Token(Matrix_get($actorSymbol(inputToken), 0, 0), Matrix_get(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen)), 0, 0), $actorSymbol(toleranceToken)))
#endif

    ) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d\n Value was: %s. Should have been within %g of %s\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($actorSymbol(inputToken)::toString())->payload.String,
            $param(tolerance),
            $tokenFunc(Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen))::toString())->payload.String);
   exit(-1);
}
/**/

/***TokenBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}
/* TBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = Array_get($param(correctValues), $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)) {
    //if ($type(input) != TYPE_Array) {
      if (!$tokenFunc($actorSymbol(inputToken)::equals(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)))) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
            $actorSymbol(numberOfTokensSeen),
            $tokenFunc($actorSymbol(inputToken)::toString())->payload.String,
            $tokenFunc(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)::toString())->payload.String);
   exit(-1);
}
/**/

/***UnsignedByteBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
$actorSymbol(numberOfTokensSeen)++;

/* UB $actorSymbol(), UnsignedByteBlock($channel) which has only one channel */
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - $param(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been between: %10.30g and %10.30g\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) -
                    $param(tolerance),
            $param(correctValues, $actorSymbol(numberOfTokensSeen)) +
                    $param(tolerance));
   exit(-1);
}
/**/

/***UnsignedByteBlockMultiChannel($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(inputToken) = $get(input#$channel);
} else {
        return;
}
if ($channel == 0) {
        $actorSymbol(numberOfTokensSeen)++;
}

/* UBMC $channel of $actorSymbol() */
$actorSymbol(correctValuesThisFiring_$channel) = $param(correctValues, $actorSymbol(numberOfTokensSeen));
if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
        && abs($actorSymbol(inputToken)
                - (($cgType(input))(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)->payload)).$lcCgType(input)Value())
        > $param(tolerance)) {
    fprintf(stderr, "\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been within %10.30g of: %d\n",
            $actorSymbol(numberOfTokensSeen),
            $actorSymbol(inputToken),
            $param(tolerance),
            (int)(Array_get($actorSymbol(correctValuesThisFiring_$channel), $channel)->payload));
   exit(-1);
}
/**/

/*** wrapupBlock ***/
if (($actorSymbol(numberOfTokensSeen) + 1) < $size(correctValues)) {
    if ($actorSymbol(numberOfTokensSeen) < 1) {
        fprintf(stderr, "\nTest received only %d tokens, yet the correctValues parameter was expecting %d tokens, exiting.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
        exit(-2);
    } else {
        fprintf(stderr, "\nTest received only %d tokens, yet the correctValues parameter was expecting %d tokens.\n", $actorSymbol(numberOfTokensSeen), $size(correctValues));
    }
}
/**/
