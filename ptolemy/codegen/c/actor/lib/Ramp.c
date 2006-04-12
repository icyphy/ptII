/***preinitBlock ($declaredType)***/
static $declaredType $actorSymbol(state);
/**/

/***CommonInitBlock***/
    $actorSymbol(state) = $val(init);
/**/

/***StringInitBlock***/
    $actorSymbol(state) = strdup($val(init));
/**/

/***IntFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/

/***DoubleFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/

/***BooleanFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) |= $val(step);
/**/

/***StringFireBlock***/
    $ref(output) = (char*) realloc($ref(output), sizeof(char) * (strlen($actorSymbol(state)) + 1) );
	strcpy($ref(output), $actorSymbol(state));
    $actorSymbol(state) = (char*) realloc($actorSymbol(state), sizeof(char) * (strlen($actorSymbol(state)) + strlen($val(step)) + 1) );
	strcat($actorSymbol(state),  $val(step));
/**/

/***TokenFireBlock***/
    $ref(output) = $actorSymbol(state);
	$actorSymbol(state) = $tokenFunc($ref(output)::add($val(step)));
/**/