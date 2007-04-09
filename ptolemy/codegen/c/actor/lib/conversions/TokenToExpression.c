/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free($ref(output));

$ref(output) = $tokenFunc($ref(input)::toString()).payload.String;
/**/

/*** TokenArrayFireBlock($elementType) ***/
$ref(output) = $tokenFunc($typeFunc(TYPE_Array::convert($ref(input), $elementType))::toString()).payload.String;
/**/

/*** FireBlock($type) ***/
$ref(output) = $typetoString($ref(input));
/**/
