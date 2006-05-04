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
    $actorSymbol(sum) += $ref(($cgType(output)) input#$channel);
/**/

/***DoubleFireBlock($channel) ***/
    $actorSymbol(sum) += $ref(($cgType(output)) input#$channel);
/**/

/***BooleanFireBlock($channel) ***/
    $actorSymbol(sum) |= $ref(($cgType(output)) input#$channel);
/**/

/***StringFireBlock($channel)***/
    $actorSymbol(sum) = (char*) realloc($actorSymbol(sum), sizeof(char) * (strlen($actorSymbol(sum)) + strlen($ref(($cgType(output)) input#$channel)) + 1) );
	strcat($actorSymbol(sum),  $ref(($cgType(output)) input#$channel));
/**/

/***TokenFireBlock($channel)***/
	$actorSymbol(sum) = $tokenFunc($actorSymbol(sum)::add($ref(($cgType(output)) input#$channel)));
/**/

/***sendBlock***/
    $ref(output) = $actorSymbol(sum);
/**/

/***initReset***/
    $actorSymbol(resetTemp) = $ref(reset#0);
/**/

/***readReset($channel)***/
    $actorSymbol(resetTemp) |= $ref(reset#$channel);
/**/

/***ifReset***/
    if ($actorSymbol(resetTemp))
/**/

/***StringWrapupBlock***/
    free($actorSymbol(sum));
/**/
