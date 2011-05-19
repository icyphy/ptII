/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
$put(output, $channel, $get(input#$channel));
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
$put(output, $channel, $new($type, $get(input#$channel)));
/**/

/*** tokenFireBlock($channel, $type) ***/
$put(output, $channel, $get(input#$channel));
/**/

/***fireBlock($channel, $type)***/
// CommutatorFireBlock()
$put(output, $channel, $get(input#$channel));
/**/
