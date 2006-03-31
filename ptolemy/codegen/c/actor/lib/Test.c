/*** preinitBlock ***/
	// Initialize to -1 because we ALWAYS increment first.
	// This is more convenient for multiport, where we check if $channel
	// number is equal zero (the first channel). If so, then we increment.
    int $actorSymbol(numberOfTokensSeen) = -1;
/**/

/***IntBlock($channel)***/
        $actorSymbol(numberOfTokensSeen)++;

        /* $actorSymbol(), IntBlock($channel) which has only one channel */
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $ref(correctValues, $actorSymbol(numberOfTokensSeen)));
            exit(-1);
        }
/**/


/***IntBlockMultiChannel($channel)***/
        if ($channel == 0) {
	        $actorSymbol(numberOfTokensSeen)++;
    	}    

        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
               $ref(correctValues, $actorSymbol(numberOfTokensSeen));
               
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Int)
                > $ref(tolerance)) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Int);
            exit(-1);    
        }        
/**/


/***DoubleBlock($channel)***/
        $actorSymbol(numberOfTokensSeen)++;

        /* $actorSymbol(), DoubleBlock($channel) which has only one channel */
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $ref(correctValues, $actorSymbol(numberOfTokensSeen))
                > $ref(tolerance))) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $ref(correctValues,
                            $actorSymbol(numberOfTokensSeen)));
            exit(-1);    
        }
/**/

/***DoubleBlockMultiChannel($channel)***/
        if ($channel == 0) {
	        $actorSymbol(numberOfTokensSeen)++;
    	}    

        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
                $ref(correctValues, $actorSymbol(numberOfTokensSeen));
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Double)
                > $ref(tolerance)) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was: %g. Should have been: %g\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Double);
            exit(-1);    
        }
/**/

/***BooleanBlock($channel)***/
        $actorSymbol(numberOfTokensSeen)++;
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && (!$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                        && $ref(input#$channel))
                || ($ref(correctValues, $actorSymbol(numberOfTokensSeen))
                        && !$ref(input#$channel)) ) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
                    $actorSymbol(numberOfTokensSeen),
                    btoa($ref(input#$channel)),
                    btoa($ref(correctValues, $actorSymbol(numberOfTokensSeen))));
            exit(-1);    		
		}
/**/

/***BooleanBlockMultiChannel($channel)***/
        if ($channel == 0) {
	        $actorSymbol(numberOfTokensSeen)++;
    	}    
        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
                $ref(correctValues, $actorSymbol(numberOfTokensSeen));
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && ((!$actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Boolean
                            && $ref(input#$channel))
                        || ($actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Boolean
                                && !$ref(input#$channel)))) {
            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
                    $actorSymbol(numberOfTokensSeen),
                    btoa($ref(input#$channel)),
                    btoa($actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.Boolean));
            exit(-1);    
        }
/**/

/***StringBlock($channel)***/
        $actorSymbol(numberOfTokensSeen)++;
		if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
	                && (strcmp($ref(correctValues, $actorSymbol(numberOfTokensSeen)),
	                            $ref(input#$channel)) != 0) ) {
	            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
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
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
               $ref(correctValues, $actorSymbol(numberOfTokensSeen));
		if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
	                && (strcmp((char *)$ref(input#$channel),
	                            $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.String)
	                != 0)) {
	            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
	                    $actorSymbol(numberOfTokensSeen),
	                    $ref(input#$channel),
	                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.String);
	            exit(-1);
		}
/**/

/***TokenBlock($channel)***/
        $actorSymbol(numberOfTokensSeen)++;
        if (!$typeFunc($ref(input#$channel)::equals($ref(correctValues, $actorSymbol(numberOfTokensSeen))))) {
            printf("\nTest $actorSymbol($channel) fails in interation %d.\n Value was: %s. Should have been: %s.\n",
                    $actorSymbol(numberOfTokensSeen),
                    $typeFunc($ref(input#$channel)::toString()),
                    $typeFunc($ref(correctValues, $actorSymbol(numberOfTokensSeen))::toString()));
            exit(-1);    				
		}
/**/

/***TokenBlockMultiChannel($channel)***/
        if ($channel == 0) {
	        $actorSymbol(numberOfTokensSeen)++;
    	}    
        /* $channel of $actorSymbol() */
        Token $actorSymbol(correctValuesThisFiring)_$channel =
               Array_get($ref(correctValues), $actorSymbol(numberOfTokensSeen));
		if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
	                && !$typeFunc($ref(input#$channel)::equals(Array_get($actorSymbol(correctValuesThisFiring)_$channel, $channel)))) {
	            printf("\nTest $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
	                    $actorSymbol(numberOfTokensSeen),
	                    $typeFunc($ref(input#$channel)::toString()),
	                    $typeFunc(Array_get($actorSymbol(correctValuesThisFiring)_$channel, $channel)::toString()));
	            exit(-1);
		}
/**/
