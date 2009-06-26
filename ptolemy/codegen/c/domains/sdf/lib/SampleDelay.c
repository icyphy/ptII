/*** initTokens($offset) ***/
        $ref(output, $offset) = $convert_$elementType(initialOutputs)_$cgType(output)($val(initialOutputs, $offset));
        $send(output, 0)
/**/

/***fireBlock***/
        $get(input, 0)
        $ref(output) = $ref(input);
        $send(output, 0)
/**/
