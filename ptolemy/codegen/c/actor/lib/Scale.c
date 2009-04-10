/***scaleOnLeft***/
    $ref(output) = $multiply_$cgType(input)_$cgType(factor)($ref(input), $ref(factor));
/**/

/***scaleOnRight***/
        $ref(output) = $multiply_$cgType(factor)_$cgType(input)($ref(factor), $ref(input));
/**/
