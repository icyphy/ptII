/***preinitBlock($type)***/
    $type $actorSymbol(previousInput);
/**/

/***initBlock***/
	$actorSymbol(previousInput) = $ref(input);
/**/

/***CommonFireBlock***/
    $actorSymbol(previousInput) =  - $actorSymbol(previousInput);
    $ref(output) = $actorSymbol(previousInput); 
/**/

/***CommonFireBlock***/
    $actorSymbol(previousInput) = $ref(input) - $actorSymbol(previousInput);
    $ref(output) = $actorSymbol(previousInput);
/**/
