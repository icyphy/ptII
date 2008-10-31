/*** initBlock ***/
$ref(output) = $new(Matrix($size(input), 1, 0));
/**/

/*** fireBlock($channel) ***/
//$ref(output).payload.Matrix->elements[$channel] = $ref(input#$channel);
Matrix_set($ref(output), $channel, 0, $ref(input#$channel));
/**/

/*** fireBlock($channel, $type) ***/
//$ref(output).payload.Matrix->elements[$channel] = $new($type($ref(input#$channel)));
Matrix_set($ref(output), $channel, 0, $new($type($ref(input#$channel))));
/**/
