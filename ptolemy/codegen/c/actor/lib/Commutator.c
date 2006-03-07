/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
    $ref(output, $channel) = $ref(input#$channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
    $token(output, $channel) = $new($type, $ref(input#$channel));
/**/

/*** tokenFireBlock($channel, $type) ***/
    $token(output, $channel) = $token(input#$channel);
/**/

