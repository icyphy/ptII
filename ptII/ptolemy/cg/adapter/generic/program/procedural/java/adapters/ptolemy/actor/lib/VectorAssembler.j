/***preinitBlock***/
$targetType(output) $actorSymbol(state) = $new(Matrix($size(input), 1, 0));
/**/

/*** initBlock ***/
// $put(output, $new(Matrix($size(input), 1, 0)));
/**/

/*** fireBlock($channel) ***/
Matrix_set($actorSymbol(state), $channel, 0, $get(input#$channel));
$put(output, $actorSymbol(state));
/**/

/*** fireBlock($channel, $type) ***/
Matrix_set($actorSymbol(state), $channel, 0, $new($type($get(input#$channel))));
$put(output, $actorSymbol(state));
/**/
