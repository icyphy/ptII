/*** initBlock ($size)***/
    $ref(output) = $new(Array($size,0));
/**/



/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
    $ref(output).payload.Array->elements[$channel] = $ref(input#$channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
    $ref(output).payload.Array->elements[$channel] = $new($type($ref(input#$channel)));
/**/

/*** tokenFireBlock($channel, $type)***/
    $ref(output).payload.Array->elements[$channel] = $ref(input#$channel);
/**/
