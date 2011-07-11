/***scaleOnLeft***/
$targetType(output) $actorSymbol(result) = $multiply_$cgType(input)_$cgType(factor)($get(input), $val(factor));
$put(output, $actorSymbol(result));
/**/

/***scaleOnRight***/
$targetType(output) $actorSymbol(result) = $multiply_$cgType(factor)_$cgType(input)($val(factor), $get(input));
$put(output, $actorSymbol(result));
/**/
