/***preinitBlock***/
// FIXME: how do we reset count?
static int $actorSymbol(count) = 0; 
/**/

/***numberBlock($channel)***/
        // Handling $actorSymbol(), which has only one channel
        if ($actorSymbol(count) <
                $size(correctValues)
                && fabs($ref(input#$channel)
                        - $ref(correctValues,
                                $actorSymbol(count)))
                > $ref(tolerance)) {
            // FIXME: what about types other than double?
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n",
                    $actorSymbol(count),
                    (double)$ref(input#$channel),
                    (double)$ref(correctValues,
                            $actorSymbol(count)));
            exit(-1);    
        }
        $actorSymbol(count) ++;

/**/

/***numberBlockMultiChannel($channel,$inputType)***/
        // Handling channel $channel of $actorSymbol()
        ArrayToken $actorSymbol(correctValuesThisFiring)_$channel = $ref(correctValues, $actorSymbol(count));
        if ($actorSymbol(count) <
                $size(correctValues)
                && fabs($ref(input#$channel)
                        - $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType)
                > $ref(tolerance)) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %f. Should have been: %f\n",
                    $actorSymbol(count),
                    (double)$ref(input#$channel),
                    $actorSymbol(correctValuesThisFiring)_$channel->elements[$channel].payload.$inputType);
            exit(-1);    
        }
        $actorSymbol(count) ++;

/**/


/***stringBlock($channel)***/
	if (strcmp($ref(correctValues,
                           $actorSymbol(count)),
                    $ref(input#$channel))) {
            printf("Test $actorSymbol($channel) fails in iteration %d.\n Value was: %s. Should have been: %s\n",
                    $actorSymbol(count),
                    $ref(input#$channel),
                    $ref(correctValues,
                            $actorSymbol(count)));
            exit(-1);    		
	}
        $actorSymbol(count) ++;
/**/

/***tokenBlock($channel)***/
	if (!$typeFunc($ref(input#$channel),
                    equals($ref(input#$channel)))) {
            printf("Test fails in iteration %d", $actorSymbol(count));
            printf(".\n Value was: ");
            $typeFunc($ref(input#$channel), print());
            printf(". Should have been: ");
            $typeFunc($ref(correctValues, $actorSymbol(count)), print());
            printf(".\n");,
            exit(-1);    				
	}
    $actorSymbol(count) ++;
/**/

