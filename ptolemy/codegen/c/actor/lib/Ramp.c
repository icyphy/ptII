/***preinitBlock ($declaredType)***/
static $declaredType $actorSymbol(state);
/**/

/***initBlock***/
    $actorSymbol(state) = $val(init);
/**/

/***IntFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/

/***BooleanFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/

/***DoubleFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/

/***StringFireBlock***/
    $ref(output) = (char*) realloc($ref(output), sizeof(char) * (strlen($actorSymbol(state)) + 1) );
	strcpy($ref(output), $actorSymbol(state));
    $actorSymbol(state) = (char*) realloc($actorSymbol(state), sizeof(char) * (strlen($actorSymbol(state)) + strlen($val(step)) + 1) );
	strcat($actorSymbol(state),  $val(step));
/**/

/***TokenFireBlock***/
    $ref(output) = $actorSymbol(state);
	$actorSymbol(state) = $typeFunc($ref(output)::add($val(step)));
/**/