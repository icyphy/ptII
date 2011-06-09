/*** initBlock ***/
$ref(output) = $new(Matrix($size(input), 1, 0));
/**/

/*** fireBlock($channel) ***/
Matrix_set($ref(output), $channel, 0, $ref(input#$channel));
/**/

/*** fireBlock($channel, $type) ***/
Matrix_set($ref(output), $channel, 0, $new($type($ref(input#$channel))));
/**/
