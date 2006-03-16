/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
    $ref(output#$channel, input) = $ref(input, $channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
    $token(output#$channel, input) = $new($type, $ref(input));
/**/

/*** tokenFireBlock($channel, $type) ***/
    $token(output#$channel, input) = $token(input);
/**/

