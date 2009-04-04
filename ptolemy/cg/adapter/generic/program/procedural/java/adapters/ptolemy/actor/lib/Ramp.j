/***preinitBlock***/
static $targetType(output) $actorSymbol(state);
/**/

/*** ArrayConvertInitBlock($elementType) ***/
$actorSymbol(state) = $typeFunc(TYPE_Array::convert($actorSymbol(state), $elementType));
/**/

/*** ArrayConvertStepBlock($elementType) ***/
$ref(step) = $typeFunc(TYPE_Array::convert($ref(step), $elementType));
/**/


/***CommonInitBlock($type)***/
$actorSymbol(state) = $val(($type)init);
/**/

/***StringInitBlock***/
$actorSymbol(state) = strdup($val((String)init));
/**/

/***IntegerFireBlock***/
$ref(output) = $actorSymbol(state);
$actorSymbol(state) += $ref((Integer)step);
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
$actorSymbol(state) = $add_Token_Token($ref(output), $ref(step));
/**/
