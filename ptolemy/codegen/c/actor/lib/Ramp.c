/***preinitBlock ($declaredType)***/
static $declaredType $actorSymbol(state);
/**/

/***CommonInitBlock($type)***/
    $actorSymbol(state) = $val(($type)init);
/**/

/***StringInitBlock***/
    $actorSymbol(state) = strdup($val((String)init));
/**/

/***IntFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $ref((Int)step);
/**/

/***DoubleFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $ref((Double)step);
/**/

/***BooleanFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) |= $ref((Boolean)step);
/**/

/***StringFireBlock***/
    $ref(output) = (char*) realloc($ref(output), sizeof(char) * (strlen($actorSymbol(state)) + 1) );
	strcpy($ref(output), $actorSymbol(state));
    $actorSymbol(state) = (char*) realloc($actorSymbol(state), sizeof(char) * (strlen($actorSymbol(state)) + strlen($ref((String)step)) + 1) );
	strcat($actorSymbol(state),  $ref((String)step));
/**/

/***TokenFireBlock***/
    $ref(output) = $actorSymbol(state);
	$actorSymbol(state) = $tokenFunc($ref(output)::add($ref((Token)step)));
/**/