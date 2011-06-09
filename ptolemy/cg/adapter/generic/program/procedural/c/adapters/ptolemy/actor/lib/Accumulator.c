/***preinitBlock ***/
static $targetType(output) $actorSymbol(sum);
/**/

/***preinitReset ***/
static boolean $actorSymbol(resetTemp);
/**/

/***InitSum***/
$actorSymbol(sum) = $val(($cgType(output)) init);
/**/

/***StringInitSum***/
$actorSymbol(sum) = strdup($val(($targetType(output)) init));
/**/

/***IntFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) += ($targetType(output)) $get(input#$channel);
}
/**/

/***DoubleFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) += ($targetType(output)) $get(input#$channel);
}
/**/

/***BooleanFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) |= ($targetType(output)) $get(input#$channel);
}
/**/

/***StringFireBlock($channel)***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) = (char*) realloc($actorSymbol(sum), sizeof(char) * (strlen($actorSymbol(sum)) + strlen(($cgType(output)) $get(input#$channel)) + 1) );
    strcat($actorSymbol(sum), ($targetType(output)) $get(input#$channel));
}
/**/

/***TokenFireBlock($channel)***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) = $tokenFunc($actorSymbol(sum)::add(($targetType(output)) $get(input#$channel)));
}
/**/

/***sendBlock***/
$put(output, $actorSymbol(sum));
/**/

/***initReset***/
if ($hasToken(reset#0)) {
    $actorSymbol(resetTemp) = $get(reset#0);
}
/**/

/***readReset($channel)***/
if ($hasToken(reset#channel)) {
    $actorSymbol(resetTemp) |= $get(reset#$channel);
}
/**/

/***ifReset***/
if ($actorSymbol(resetTemp)) {
/**/

/***StringWrapupBlock***/
free($actorSymbol(sum));
/**/
