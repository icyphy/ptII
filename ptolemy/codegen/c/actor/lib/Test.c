/***IntBlock($channel)***/
    /* $actorSymbol(), which has only one channel */
    if (iteration < $size(correctValues) && fabs($ref(input#$channel) - $ref(correctValues, iteration)) > $ref(tolerance)) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n", iteration, $ref(input#$channel), $ref(correctValues, iteration));
        exit(-1);
    }
/**/


/***IntBlockMultiChannel($channel,$inputType)***/
    /* $channel of $actorSymbol() */
    ArrayToken $actorSymbol(correctValuesThisFiring)_$channel = $ref(correctValues, iteration);
    if (iteration < $size(correctValues) && fabs($ref(input#$channel) - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType) > $ref(tolerance)) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n", iteration, $ref(input#$channel), $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
        exit(-1);    
    }
/**/


/***DoubleBlock($channel)***/
    /* $actorSymbol(), which has only one channel */
    if (iteration < $size(correctValues) && fabs($ref(input#$channel) - $ref(correctValues, iteration)) > $ref(tolerance)) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n", iteration, $ref(input#$channel), $ref(correctValues, iteration));
        exit(-1);    
    }
/**/

/***DoubleBlockMultiChannel($channel,$inputType)***/
    /* $channel of $actorSymbol() */
    ArrayToken $actorSymbol(correctValuesThisFiring)_$channel = $ref(correctValues, iteration);
    if (iteration < $size(correctValues) && fabs($ref(input#$channel) - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType) > $ref(tolerance)) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %g. Should have been: %g\n", iteration, $ref(input#$channel), $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
        exit(-1);    
    }
/**/

/***BooleanBlock($channel)***/
    if (iteration < $size(correctValues) && (!$ref(correctValues, iteration) && $ref(input#$channel)) || ($ref(correctValues, iteration) && !$ref(input#$channel)) ) {
        printf("Test fails in iteration %d.\n Value was: %s. Should have been: %s\n", iteration, btoa($ref(input#$channel)), btoa($ref(correctValues, iteration)));
        exit(-1);    		
	}
/**/

/***BooleanBlockMultiChannel($channel,$inputType)***/
    /* $channel of $actorSymbol() */
    ArrayToken $actorSymbol(correctValuesThisFiring)_$channel = $ref(correctValues, iteration);
    if (iteration < $size(correctValues) &&
     (!$ref(correctValues, iteration) && $ref(input#$channel)) ||
     ($ref(correctValues, iteration) && !$ref(input#$channel)) ) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n",
                iteration,
                (double)$ref(input#$channel),
                $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
        exit(-1);    
    }
/**/

/***StringBlock($channel)***/
	if (iteration < $size(correctValues) && (strcmp($ref(correctValues, iteration), $ref(input#$channel)) != 0) ) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n", iteration, $ref(input#$channel), $ref(correctValues, iteration));
        exit(-1);    		
	}
/**/

/***StringBlockMultiChannel($channel,$inputType)***/
    /* $channel of $actorSymbol() */
    ArrayToken $actorSymbol(correctValuesThisFiring)_$channel = $ref(correctValues, iteration);

	if (iteration < $size(correctValues) && (strcmp((char *)$ref(input#$channel), $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType)) != 0) {
        printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n", iteration, $ref(input#$channel), $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
        exit(-1);
	}
/**/

/***TokenBlock($channel)***/
	if (!$typeFunc($ref(input#$channel)::equals($ref(input#$channel)))) {
        printf("Test fails in iteration %d.\n Value was: %s. Should have been: %s.\n", iteration, $typeFunc($ref(input#$channel)::toString()), $typeFunc($ref(correctValues, iteration)::toString()));
        exit(-1);    				
	}
/**/

