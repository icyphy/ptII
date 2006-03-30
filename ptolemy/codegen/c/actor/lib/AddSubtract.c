/***preinitBlock***/
	int $actorSymbol(i);
	int $actorSymbol(length);
/**/

/***NumericAddBlock($width)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$ref(output) += $ref(plus#$actorSymbol(i));
	}
/**/

/***BooleanAddBlock($width)***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$ref(output) |= $ref(plus#$actorSymbol(i));
	}
/**/

/***StringAddBlock($width)***/
	$actorSymbol(length) = 1;		// null terminator.
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$actorSymbol(length) += strlen($ref(plus#$actorSymbol(i)));
	}
	
	$ref(output) = (char*) realloc($ref(output), $actorSymbol(length));
	$ref(output) = strcpy($ref(plus#0));
	
	for ($actorSymbol(i) = 1; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		strcat($ref(output), $ref(plus#$actorSymbol(i)));
	}	
/**/

/***TokenAddBlock($width)***/
	$ref(output) = $ref(plus#0);
	
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$typeFunc($ref(output)::add($ref(plus#$actorSymbol(i))));
	}
/**/

/***NumericMinusBlock***/
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$ref(output) -= $ref(minus#$actorSymbol(i));
	}
/**/

/***TokenMinusBlock***/
	$ref(output) = $ref(minus#0);
	
	for ($actorSymbol(i) = 1; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$ref(output) = $typeFunc($ref(output)::substract($ref(minus#$actorSymbol(i))));
	}
/**/
