/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** fireBlock ***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
		$ref(output, $actorSymbol(i)) = $ref(input, $actorSymbol(i));
	}
/**/

