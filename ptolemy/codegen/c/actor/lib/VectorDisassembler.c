/*** fireBlock($channel) ***/
$ref(output#$channel) = Matrix_get($ref(input), $channel, 0);
/**/

/*** fireBlock($channel, $type) ***/
$ref(output#$channel) = Matrix_get($ref(input), $channel, 0).payload.$type;
/**/
