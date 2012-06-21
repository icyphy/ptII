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

/*** FireBlock($type1) ***/
Token foo = $new(Object(null));
$put(output, $$type1toString($get(input)));
//$put(output, (String)($tokenFunc($get(input)::toString()).payload));

/**/
