/***preinitBlock***/
static Token $actorSymbol(state);
/**/

/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free(put(output));

$put(output, (String)($Array_toString($get(input)).getPayload()));

/**/

/*** TokenArrayFireBlock($elementType) ***/
$actorSymbol(state) = $typeFunc(TYPE_Array::convert($get(input), $elementType));
$put(output, (String)($tokenFunc($actorSymbol(state)::toString()).payload));
/**/

/*** FireBlock($type) ***/
$put(output, $typetoString($get(input)));
/**/

/*** ObjectFireBlock($type1) ***/
$put(output, (String)$typeFunc($type(input)::toString($get(input))).payload);
/**/
