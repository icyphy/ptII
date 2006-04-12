/***preinitBlock***/
	int $actorSymbol(i);
	int $actorSymbol(length);
	Token $actorSymbol(result);
/**/

/***PreFireBlock***/
	$ref(output) = $ref(plus#0);
/**/

/***MinusPreFireBlock***/
	$ref(output) = -$ref(minus#0);
/**/

/***TokenPreFireBlock***/
	$actorSymbol(result) = $ref(plus#0);
/**/

/***TokenMinusPreFireBlock***/
	$actorSymbol(result) = $tokenFunc($ref(minus#0)::negate());
/**/


/***IntAddBlock($channel)***/
	$ref(output) += $ref(plus#$channel);
/**/

/***IntMinusBlock($channel)***/
	$ref(output) -= $ref(minus#$channel);
/**/

/***DoubleAddBlock($channel)***/
	$ref(output) += $ref(plus#$channel);
/**/

/***DoubleMinusBlock($channel)***/
	$ref(output) -= $ref(minus#$channel);
/**/


/***BooleanAddBlock($channel)***/
	$ref(output) |= $ref(plus#$actorSymbol(i));
/**/



/***StringPreFireBlock***/
	$actorSymbol(length) = 1;		// null terminator.
/**/

/***StringLengthBlock($channel)***/
	$actorSymbol(length) += strlen($ref(plus#$channel));	
/**/

/***StringAllocBlock***/
	$ref(output) = (char*) realloc($ref(output), $actorSymbol(length));
	strcpy($ref(output), $ref(plus#0));	
/**/


/***StringAddBlock($channel)***/
	strcat($ref(output), $ref(plus#$channel));
/**/


// FIXME: have to deallocate the tokens.
/***TokenAddBlock($channel)***/
	$actorSymbol(result) = $tokenFunc($actorSymbol(result)::add($ref(plus#$channel)));
/**/

/***TokenMinusBlock($channel)***/
	$actorSymbol(result) = $tokenFunc($actorSymbol(result)::substract($ref(minus#$channel)));
/**/

/***TokenPostFireBlock***/
	$ref(output) = $actorSymbol(result);
/**/













/***NumericFireBlock***/
	$ref(output) = $ref(plus#0);
	for ($actorSymbol(i) = 1; $actorSymbol(i) < $size(plus); $actorSymbol(i)++) {
		$ref(output) += $ref(plus#$actorSymbol(i));
	}
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(minus); $actorSymbol(i)++) {
		$ref(output) -= $ref(minus#$actorSymbol(i));
	}
/**/

/***BooleanFireBlock($width)***/
	$ref(output) = $ref(plus#0);
	for ($actorSymbol(i) = 1; $actorSymbol(i) < $width; $actorSymbol(i)++) {
		$ref(output) |= $ref(plus#$actorSymbol(i));
	}
/**/

/***StringFireBlock($width)***/
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

/***TokenFireBlock($width)***/
	$ref(output) = $ref(plus#0);
	
	for ($actorSymbol(i) = 1; $actorSymbol(i) < $size(plus); $actorSymbol(i)++) {
		$tokenFunc($ref(output)::add($ref(plus#$actorSymbol(i))));
	}
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(minus); $actorSymbol(i)++) {
		$ref(output) = $tokenFunc($ref(output)::substract($ref(minus#$actorSymbol(i))));
	}
/**/
