/***CommonPreinitBlock($type)***/
    $type $actorSymbol(_currentInput) = 0;
    $type $actorSymbol(_lastInput) = 0;
/**/

/***CommonFireBlock***/
    $actorSymbol(_currentInput) = $ref(input);
    if ($actorSymbol(_lastInput) != 0) {
	    $ref(output) = $actorSymbol(_currentInput) - $actorSymbol(_lastInput);
    } else {
	    $ref(output) = $actorSymbol(_currentInput);
    }
    $actorSymbol(_lastInput) = $actorSymbol(_currentInput);
/**/



/***TokenPreinitBlock***/
    Token $actorSymbol(_currentInput) = (Token) NULL;
    Token $actorSymbol(_lastInput) = (Token) NULL;
/**/

/***TokenFireBlock***/
    $actorSymbol(_currentInput) = $ref(input);
    if ($actorSymbol(_lastInput) != NULL) {
	    $ref(output) = $tokenFunc($actorSymbol(_currentInput)::substract($actorSymbol(_lastInput)));
    } else {
	    $ref(output) = $actorSymbol(_currentInput);
    }
    $actorSymbol(_lastInput) = $actorSymbol(_currentInput);
/**/
