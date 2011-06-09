/*** initTokens($offset) ***/
        $ref(output, $offset) = $val(initialOutputs, $offset);
        $send(output, 0)
/**/

/***fireBlock***/
        $get(input, 0)
        $ref(output) = $ref(input);
        $send(output, 0)
/**/
