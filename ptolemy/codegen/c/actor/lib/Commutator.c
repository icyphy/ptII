/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
$ref(output, $channel) = $ref(input#$channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
$ref(output, $channel) = $new($type, $ref(input#$channel));
/**/

/*** tokenFireBlock($channel, $type) ***/
$ref(output, $channel) = $ref(input#$channel);
/**/

/*** fireBlock($channel, $type) ***/
$ref(output, $channel) = $ref(($type) input#$channel);
/**/
