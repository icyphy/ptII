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
$actorSymbol(sum) = $val(($cgType(output)) init);
/**/

/***intFireBlock($channel) ***/
$actorSymbol(sum) += $get(input#$channel);
/**/

/***doubleFireBlock($channel) ***/
$actorSymbol(sum) += $get(input#$channel);
/**/

/***booleanFireBlock($channel) ***/
$actorSymbol(sum) |= $get(input#$channel);
/**/

/***StringFireBlock($channel)***/
$actorSymbol(sum) += $get(input#$channel);
/**/

/***TokenFireBlock($channel)***/
$actorSymbol(sum) = $tokenFunc($actorSymbol(sum)::add($get(input#$channel)));
/**/

/***sendBlock***/
$put(output, $actorSymbol(sum));
/**/

/***initReset***/
$actorSymbol(resetTemp) = $get(reset#0);
/**/

/***readReset($channel)***/
$actorSymbol(resetTemp) |= $get(reset#$channel);
/**/

/***ifReset***/
if ($actorSymbol(resetTemp)) {
/**/

/***StringWrapupBlock***/
        free($actorSymbol(sum));
/**/
