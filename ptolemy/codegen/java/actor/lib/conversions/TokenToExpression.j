/*** TokenFireBlock ***/
// FIXME: is this the proper way to free the allocated space?
//free($ref(output));

$ref(output) = ($Array_toString($ref(input)).payload);

/**/

/*** TokenArrayFireBlock($elementType) ***/
$ref(output) = $tokenFunc($typeFunc(TYPE_Array::convert($ref(input), $elementType))::toString()).payload.String;
/**/

/*** FireBlock($type) ***/
$ref(output) = $typetoString($ref(input));
/**/
