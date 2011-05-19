/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free($ref(output));

$ref(output) = (String)($Array_toString($ref(input)).payload);

/**/

/*** TokenArrayFireBlock($elementType) ***/
$ref(output) = (String)($tokenFunc($typeFunc(TYPE_Array::convert($ref(input), $elementType))::toString()).payload);
/**/

/*** FireBlock($type) ***/
$ref(output) = $typetoString($ref(input));
/**/
