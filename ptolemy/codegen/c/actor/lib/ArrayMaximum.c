/*** preinitBlock ($type)***/
	int $actorSymbol(i);
	int $actorSymbol(indexValue);
	$type $actorSymbol(currentMax);
	$type $actorSymbol(temp);
/**/

/*** fireBlock ***/
    $actorSymbol(indexValue) = 0;
    $actorSymbol(currentMax) = Array_get($ref(input), 0).payload.$cgType(output);

	for ($actorSymbol(i) = 1; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
        $actorSymbol(temp) = Array_get($ref(input), $actorSymbol(i)).payload.$cgType(output);

        if ($actorSymbol(currentMax) < $actorSymbol(temp)) {
            $actorSymbol(indexValue) = $actorSymbol(i);
            $actorSymbol(currentMax) = $actorSymbol(temp);
        }
    }

    $ref(output) = $actorSymbol(currentMax);
    $ref(index) = $actorSymbol(indexValue);
/**/
