/*** preinitBlock ***/
	int $actorSymbol(length);
	int $actorSymbol(i);
	int $actorSymbol(j);
	
	boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
	$actorSymbol(doDelete) = false;
/**/

/*** fireBlock ***/
	if ($actorSymbol(doDelete)) {
		Array_delete($ref(output));
	}
	
	$actorSymbol(length) = 0;
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(input); $actorSymbol(i)++) {
		$actorSymbol(length) += $ref(input).payload.Array->size;
	}
	
	$ref(output) = $new(Array($actorSymbol(length), 0));
	
	$actorSymbol(length) = 0;
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(input); $actorSymbol(i)++) {
		for ($actorSymbol(j) = 0; $actorSymbol(j) < $ref(input#$actorSymbol(i)).payload.Array->size; $actorSymbol(j)++, $actorSymbol(length)++) {
			$ref(output).payload.Array->elements[$actorSymbol(length)] = $tokenFunc(Array_get($ref(input#$actorSymbol(i)), $actorSymbol(length))::clone());
		}
	}
	
	$actorSymbol(doDelete) = true;
/**/

