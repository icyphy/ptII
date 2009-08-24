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
$actorSymbol(sum) = strdup($val(($cgType(output)) init));
/**/

/***IntFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) += $get(($cgType(output)) input#$channel);
}
/**/

/***DoubleFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) += $get(($cgType(output)) input#$channel);
}
/**/

/***BooleanFireBlock($channel) ***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) |= $get(($cgType(output)) input#$channel);
}
/**/

/***StringFireBlock($channel)***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) = (char*) realloc($actorSymbol(sum), sizeof(char) * (strlen($actorSymbol(sum)) + strlen($get(($cgType(output)) input#$channel)) + 1) );
    strcat($actorSymbol(sum),  $get(($cgType(output)) input#$channel));
}
/**/

/***TokenFireBlock($channel)***/
if ($hasToken(input#$channel)) {
    $actorSymbol(sum) = $tokenFunc($actorSymbol(sum)::add($get(($cgType(output)) input#$channel)));
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
if ($actorSymbol(resetTemp))
        /**/

        /***StringWrapupBlock***/
        free($actorSymbol(sum));
/**/
