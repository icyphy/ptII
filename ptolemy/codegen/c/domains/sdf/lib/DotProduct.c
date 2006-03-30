/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** fireBlock ($type1, $type2)***/
	// Assume both $ref(input1) and $ref(input2) are array type.

	$ref(output) = 0;
	
	for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input1).payload.Array->size; $actorSymbol(i)++) {
		$ref(output) += Array_get($ref(input1), $actorSymbol(i)).payload.$type1 * Array_get($ref(input2), $actorSymbol(i)).payload.$type2;
	}
/**/

