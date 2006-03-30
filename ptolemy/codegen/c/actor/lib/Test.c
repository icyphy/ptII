/*** preinitBlock ***/
    int $actorSymbol(numberOfTokensSeen) = 0;
/**/

/***IntBlock($channel)***/
        /* $actorSymbol(), IntBlock($channel) which has only one channel */
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $ref(correctValues, $actorSymbol(numberOfTokensSeen)))
                > $ref(tolerance)) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $ref(correctValues, $actorSymbol(numberOfTokensSeen)));
            exit(-1);
        }
        $actorSymbol(numberOfTokensSeen)++;
/**/


/***IntBlockMultiChannel($channel,$inputType)***/
        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
               $ref(correctValues, $actorSymbol(numberOfTokensSeen));
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType)
                > $ref(tolerance)) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %d. Should have been: %d\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
            exit(-1);    
        }
        $actorSymbol(numberOfTokensSeen)++;
/**/


/***DoubleBlock($channel)***/
        /* $actorSymbol(), DoubleBlock($channel) which has only one channel */
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $ref(correctValues, $actorSymbol(numberOfTokensSeen))
                > $ref(tolerance))) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $ref(correctValues,
                            $actorSymbol(numberOfTokensSeen)));
            exit(-1);    
        }
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***DoubleBlockMultiChannel($channel,$inputType)***/
        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
                $ref(correctValues, $actorSymbol(numberOfTokensSeen));
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && fabs($ref(input#$channel)
                        - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType)
                > $ref(tolerance)) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %g. Should have been: %g\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
            exit(-1);    
        }
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***BooleanBlock($channel)***/
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && (!$ref(correctValues, $actorSymbol(numberOfTokensSeen))
                        && $ref(input#$channel))
                || ($ref(correctValues, $actorSymbol(numberOfTokensSeen))
                        && !$ref(input#$channel)) ) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
                    $actorSymbol(numberOfTokensSeen),
                    btoa($ref(input#$channel)),
                    btoa($ref(correctValues, $actorSymbol(numberOfTokensSeen))));
            exit(-1);    		
	}
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***BooleanBlockMultiChannel($channel,$inputType)***/
        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
                $ref(correctValues, $actorSymbol(numberOfTokensSeen));
        if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && ((!$actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType
                            && $ref(input#$channel))
                        || ($actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType
                                && !$ref(input#$channel)))) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a boolean of value: %s. Should have been a boolean of value: %s\n",
                    $actorSymbol(numberOfTokensSeen),
                    btoa($ref(input#$channel)),
                    btoa($actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType));
            exit(-1);    
        }
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***StringBlock($channel)***/
	if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && (strcmp($ref(correctValues, $actorSymbol(numberOfTokensSeen)),
                            $ref(input#$channel)) != 0) ) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $ref(correctValues,
                            $actorSymbol(numberOfTokensSeen)));
            exit(-1);    		
	}
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***StringBlockMultiChannel($channel,$inputType)***/
        /* $channel of $actorSymbol() */
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel =
               $ref(correctValues, $actorSymbol(numberOfTokensSeen));
	if ($actorSymbol(numberOfTokensSeen) < $size(correctValues)
                && (strcmp((char *)$ref(input#$channel),
                            $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType)
                != 0)) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was a String: \"%s\". Should have been a String: \"%s\"\n",
                    $actorSymbol(numberOfTokensSeen),
                    $ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
            exit(-1);
	}
        $actorSymbol(numberOfTokensSeen)++;
/**/

/***TokenBlock($channel)***/
        if (!$typeFunc($ref(input#$channel)::equals($ref(input#$channel)))) {
            printf("Test $actorSymbol($channel) fails in interation %d.\n Value was: %s. Should have been: %s.\n",
                    $actorSymbol(numberOfTokensSeen),
                    $typeFunc($ref(input#$channel)::toString()),
                    $typeFunc($ref(correctValues, $actorSymbol(numberOfTokensSeen))::toString()));
            exit(-1);    				
	}
        $actorSymbol(numberOfTokensSeen)++;
/**/

