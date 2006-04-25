/*** preinitBlock ($type)***/
	int $actorSymbol(i);
	int $actorSymbol(indexValue);
	$type $actorSymbol(currentMax);
	$type $actorSymbol(temp);
/**/

/*** fireBlock ***/
    $actorSymbol(indexValue) = 0;
    $actorSymbol(currentMax) = $ref(input, 0);

	for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
        $actorSymbol(temp) = $ref(token, $actorSymbol(i));

        if ($actorSymbol(currentMax) < $actorSymbol(temp)) {
            $actorSymbol(indexValue) = $actorSymbol(i);
            $actorSymbol(currentMax) = $actorSymbol(temp);
        }
    }

    $ref(output) = $actorSymbol(currentMax);
    $ref(index) = $actorSymbol(indexValue);
/**/
