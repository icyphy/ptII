/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
$put(output#$channel, $get(input#$channel));
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
$put(output#$channel, $new($type, $get(input#$channel)));
/**/

/*** tokenFireBlock($channel, $type) ***/
$put(output#$channel, $get(input#$channel));
/**/

/***fireBlock($channel, $type)***/
//put(output, $channel) = get(($type) input#$channel);

$put(output#$channel, $get(input#$channel));
/**/
