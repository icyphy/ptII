/***preinitBlock***/
static $targetType(output) $actorSymbol(state);
int $actorSymbol(LimitedFiringSource_iterationCount) = 0;
/**/

/*** ArrayConvertInitBlock($elementType) ***/
$actorSymbol(state) = $typeFunc(TYPE_Array::convert($actorSymbol(state), $elementType));
/**/

/*** ArrayConvertStepBlock($elementType) ***/
$param(step) = $typeFunc(TYPE_Array::convert($param(step), $elementType)));
/**/

/***CommonInitBlock($type)***/
$actorSymbol(state) = $val(($type)init);
/**/

/***StringInitBlock***/
$actorSymbol(state) = $val((String)init);
/**/

/***ComplexFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) = $add_Token_Token($actorSymbol(state), $param(step));
/**/

/***IntegerFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) += (Integer)$param(step);
/**/

/***DoubleFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) += $convert_$cgType(step)_Double($param(step));
/**/

/***BooleanFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) |= (Boolean)$param(step);
/**/

/***StringFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) = $actorSymbol(state) + $param(step);
/**/

/***TokenFireBlock***/
$put(output, $actorSymbol(state));
$param(step) = $val(($cgType(step)) step);
$actorSymbol(state) = $add_Token_Token($actorSymbol(state), $param(step));
/**/

/*** postfireBlock() ***/
$actorSymbol(LimitedFiringSource_iterationCount)++;
if ($val(firingCountLimit) == $actorSymbol(LimitedFiringSource_iterationCount)) {
   // Return from run()
   return false;
}
/**/
