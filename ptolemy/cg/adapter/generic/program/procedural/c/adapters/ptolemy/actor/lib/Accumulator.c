/***preinitBlock ***/
$targetType(output) $actorSymbol(sum);
/**/

/***preinitReset ***/
boolean $actorSymbol(resetTemp);
/**/

/***InitSum***/
$actorSymbol(sum) = $val(($cgType(output)) init);
/**/

/***StringInitSum***/
$actorSymbol(sum) = $val(($cgType(output)) init);
/**/

/***intFireBlock($channel) ***/
if ($hasToken(input#$channel))
        $actorSymbol(sum) += $get(input#$channel);
/**/

/***doubleFireBlock($channel) ***/
if ($hasToken(input#$channel))
        $actorSymbol(sum) += $get(input#$channel);
/**/

/***booleanFireBlock($channel) ***/
if ($hasToken(input#$channel))
        $actorSymbol(sum) |= $get(input#$channel);
/**/

/***StringFireBlock($channel)***/
if ($hasToken(input#$channel))
        $actorSymbol(sum) += $get(input#$channel);
/**/

/***TokenFireBlock($channel)***/
if ($hasToken(input#$channel))
        $actorSymbol(sum) = $tokenFunc($actorSymbol(sum)::add($get(input#$channel)));
/**/

/***sendBlock***/
$put(output, $actorSymbol(sum));
/**/

/***initReset***/
$actorSymbol(resetTemp) = $get(reset#0);
/**/

/***readReset($channel)***/
if ($hasToken(reset#$channel))
        $actorSymbol(resetTemp) |= $get(reset#$channel);
/**/

/***ifReset***/
if ($actorSymbol(resetTemp)) {
/**/

/***StringWrapupBlock***/
        free($actorSymbol(sum));
/**/
