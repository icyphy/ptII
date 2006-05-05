/*** preinitBlock ***/
	unsigned char* $actorSymbol(byteValues);
	int $actorSymbol(i);
	int $actorSymbol(length);
/**/

/*** initBlock ***/
	$actorSymbol(byteValues) = NULL;
/**/

/*** fireBlock***/
	$actorSymbol(length) = strlen($ref(input));
	$actorSymbol(byteValues) = (unsigned char*) realloc($actorSymbol(byteValues), sizeof(unsigned char) * $actorSymbol(length));
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
		$actorSymbol(byteValues)[$actorSymbol(i)] = $ref(input)[$actorSymbol(i)];
	}
	$ref(output) = $actorSymbol(byteValues);
/**/

/*** wrapupBlock ***/
	free($actorSymbol(byteValues));
/**/
