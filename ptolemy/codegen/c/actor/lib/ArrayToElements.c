/*** TokenFireBlock($channel) ***/
$ref(output#$channel) = Array_get($ref(input), $channel);
/**/

/*** PrimitiveFireBlock($channel) ***/
$ref(output#$channel) = Array_get($ref(input), $channel).payload.$cgType(output);
/**/
