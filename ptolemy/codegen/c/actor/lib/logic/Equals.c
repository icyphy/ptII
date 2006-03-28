/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** fireBlockOpen ***/
	$ref(output) = true;	
/**/

/*** EqualsBlock ($channel0, $channel1) ***/
    if ($ref(input#$channel0)!= $ref(input#$channel1)) {
	    $ref(output) = false;
    }
/**/

/*** BooleanEqualsBlock ($channel0, $channel1) ***/
    if ( (!$ref(input#$channel0) && $ref(input#$channel1)) ||
    	 ($ref(input#$channel0) && !$ref(input#$channel1)) ) {
	    $ref(output) = false;
    }
/**/

/*** StringEqualsBlock ($channel0, $channel1) ***/
    if (strcmp($ref(input#$channel0), $ref(input#$channel1)) != 0) {
	    $ref(output) = false;
	}
/**/

/*** TokenEqualsBlock ($channel0, $channel1)***/
    if ($typeFunc($ref(input#$channel0), equals($ref(input#$channel1))).payload.Boolean) {
	    $ref(output) = false;
    }
/**/

