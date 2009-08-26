/***scaleOnLeft***/
    $put(output, $multiply_$cgType(input)_$cgType(factor)($get(input), $val(factor)));
/**/

/***scaleOnRight***/
    $put(output, $multiply_$cgType(factor)_$cgType(input)($val(factor), $get(input)));
/**/
