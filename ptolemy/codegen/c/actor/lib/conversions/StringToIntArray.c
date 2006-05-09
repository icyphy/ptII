/*** preinitBlock ***/
	int $actorSymbol(i);
	int $actorSymbol(length);
	boolean $actorSymbol(doDelete) = false;
	Token $actorSymbol(intArray);
/**/

/*** fireBlock***/
	if ($actorSymbol(doDelete)) {
		Array_delete($actorSymbol(intArray));
	} else {
		$actorSymbol(doDelete) = true;
	}
	
	$actorSymbol(length) = strlen($ref(input));
	$actorSymbol(intArray) = $new(Array($actorSymbol(length), 0));
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
		$actorSymbol(intArray).payload.Array->elements[$actorSymbol(i)] = $new(Int((int) $ref(input)[$actorSymbol(i)]));
	}
	$ref(output) = $actorSymbol(intArray);
/**/
