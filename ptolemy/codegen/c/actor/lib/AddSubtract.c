/***preinitBlock($type)***/
	//int $actorSymbol(i);
	int $actorSymbol(length);
	$type $actorSymbol(result);
/**/

/***PreFireBlock***/
	$actorSymbol(result) = $ref(plus#0);
/**/

/***MinusPreFireBlock***/
	$actorSymbol(result) = -$ref(minus#0);
/**/

/***TokenPreFireBlock***/
	$actorSymbol(result) = $ref(plus#0);
/**/

/***TokenMinusPreFireBlock***/
	$actorSymbol(result) = $tokenFunc($ref(minus#0)::negate());
/**/


/***IntAddBlock($channel)***/
	$actorSymbol(result) += $ref(plus#$channel);
/**/

/***IntMinusBlock($channel)***/
	$actorSymbol(result) -= $ref(minus#$channel);
/**/

/***DoubleAddBlock($channel)***/
	$actorSymbol(result) += $ref(plus#$channel);
/**/

/***DoubleMinusBlock($channel)***/
	$actorSymbol(result) -= $ref(minus#$channel);
/**/


/***BooleanAddBlock($channel)***/
	$actorSymbol(result) |= $ref(plus#$channel);
/**/



/***StringPreFireBlock***/
	$actorSymbol(length) = 1;		// null terminator.
/**/

/***StringLengthBlock($channel)***/
	$actorSymbol(length) += strlen($ref(plus#$channel));	
/**/

/***StringAllocBlock***/
	$actorSymbol(result) = (char*) realloc($ref(output), $actorSymbol(length));
	strcpy($actorSymbol(result), $ref(plus#0));	
/**/


/***StringAddBlock($channel)***/
	strcat($actorSymbol(result), $ref(plus#$channel));
/**/


// FIXME: have to deallocate the tokens.
/***TokenAddBlock($channel)***/
	$actorSymbol(result) = $tokenFunc($actorSymbol(result)::add($ref(plus#$channel)));
/**/

/***TokenMinusBlock($channel)***/
	$actorSymbol(result) = $tokenFunc($actorSymbol(result)::substract($ref(minus#$channel)));
/**/

/***PostFireBlock***/
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
