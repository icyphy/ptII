/*** preinitBlock ***/
	int $actorSymbol(length);
	int $actorSymbol(i);
	boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
	$actorSymbol(doDelete) = false;
/**/

/*** preFire ***/
	if ($actorSymbol(doDelete)) {
		Array_delete($ref(output));
	}	
	$actorSymbol(length) = 0;
/**/


/*** getTotalLength($channel) ***/
	$actorSymbol(length) += $ref(input#$channel).payload.Array->size;
/**/

/*** allocNewArray ***/
	$ref(output) = $new(Array($actorSymbol(length), 0));	
	$actorSymbol(length) = 0;
/**/

/*** fillArray($channel) ***/	
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input#$channel).payload.Array->size; $actorSymbol(i)++) {
		$ref(output).payload.Array->elements[$actorSymbol(length)] = $tokenFunc(Array_get($ref(input#$channel), $actorSymbol(i))::clone());
		$actorSymbol(length)++;
	}
/**/

/***doDelete***/	
	$actorSymbol(doDelete) = true;
/**/

