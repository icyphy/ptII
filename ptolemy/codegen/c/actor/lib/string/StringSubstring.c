/*** preinitBlock ***/
	int $actorSymbol(length);
/**/

/*** initBlock ***/
	$ref(output) = NULL;
/**/

/*** fireBlock ***/
	$actorSymbol(length) = $ref(stop) - $ref(start) + 1;
	$ref(output) = (char*) realloc($ref(output), $actorSymbol(length));
	$ref(output) = strncpy($ref(output), $ref(input) + $ref(start), $actorSymbol(length));
/**/

