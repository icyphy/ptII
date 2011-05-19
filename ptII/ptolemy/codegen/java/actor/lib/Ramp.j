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
$actorSymbol(state) = $val((String)init);
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
$ref(output) = $actorSymbol(state);
$actorSymbol(state) = $actorSymbol(state) + $ref((String)step);
/**/

/***TokenFireBlock***/
$ref(output) = $actorSymbol(state);
$actorSymbol(state) = $add_Token_Token($ref(output), $ref(step));
/**/
