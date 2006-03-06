/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** ValueEqualsBlock ($inputWidth)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $inputWidth - 1; $actorSymbol(i)++) {
		if ($ref(input#$actorSymbol(i)) != $ref(input#$actorSymbol(i) + 1)) {
			$ref(output) = false;
			break;
		}	
	}
	$ref(output) = true;
/**/

/*** StringEqualsBlock ($inputWidth)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $inputWidth - 1; $actorSymbol(i)++) {
		if (strcmp($ref(input#$actorSymbol(i)), $ref(input#$actorSymbol(i) + 1)) {
			$ref(output) = false;
			break;
		}	
	}
	$ref(output) = true;	
/**/

/*** TokenEqualsBlock ($inputWidth)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $inputWidth - 1; $actorSymbol(i)++) {
	    if ($typeFunc($token(input, $actorSymbol(i)), equals(), $token(input, $actorSymbol(i) + 1)) {
			$ref(output) = false;
			break;
		}	
	}
	$ref(output) = true;	
    printf("\n");
/**/


// If Generator does type resolve, the generated code looks like this.
/*** EqualsBlock ($inputWidth)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $inputWidth - 1; $actorSymbol(i)++) {
		if ($ref(input#$actorSymbol(i)) != $ref(input#$actorSymbol(i) + 1)) {
			$ref(output) = false;
			break;
		}	
	}
	$ref(output) = true;	
    printf("\n");
/**/
