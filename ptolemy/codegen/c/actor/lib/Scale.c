/***scaleOnLeft***/
    $ref(output) = $multiply_$cgType(input)_$cgType(factor)($ref(input), $val(factor));
/**/

/***scaleOnRight***/
    $ref(output) = $multiply_$cgType(factor)_$cgType(input)($val(factor), $ref(input));
/**/
