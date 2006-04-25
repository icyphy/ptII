/*** preinitBlock ***/
	int* $actorSymbol(intValues);
	int $actorSymbol(i);
	int $actorSymbol(length);
/**/

/*** initBlock ***/
	$actorSymbol(intValues) = NULL;
/**/

/*** fireBlock***/
	$actorSymbol(length) = strlen($ref(input));
	$actorSymbol(intValues) = (int*) realloc($actorSymbol(intValues), sizeof(int) * $actorSymbol(length));
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
		$actorSymbol(intValues)[$actorSymbol(i)] = $ref(input)[$actorSymbol(i)];
	}
	$ref(output) = $actorSymbol(intValues);
/**/

/*** wrapupBlock ***/
	free($actorSymbol(intValues));
/**/
