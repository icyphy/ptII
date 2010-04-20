/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free($put(output));

$put(output, (String)($Array_toString($get(input)).payload));

/**/

/*** TokenArrayFireBlock($elementType) ***/
$put(output, (String)($tokenFunc($typeFunc(TYPE_Array::convert($get(input), $elementType))::toString()).payload));
/**/

/*** FireBlock($type) ***/
$put(output, $typetoString($get(input)));
/**/
