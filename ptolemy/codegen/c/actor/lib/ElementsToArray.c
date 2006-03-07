/*** initBlock ($size)***/
    $token(output) = $new(Array($size,0));
    //$ref(output) = $new(Array, $size);
/**/



/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
    $token(output).payload.Array->elements[$channel] = $ref(input#$channel);
    //$ref(output).payload.Array->elements[$channel] = $ref(input#$channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
    $token(output).payload.Array->elements[$channel] = $new($type($ref(input#$channel)));
/**/

/*** tokenFireBlock($channel, $type)***/
    $token(output).payload.Array->elements[$channel] = $token(input#$channel);
/**/
