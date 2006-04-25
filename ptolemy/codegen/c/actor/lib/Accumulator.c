/***preinitBlock ($declaredType)***/
static $declaredType $actorSymbol(sum);
/**/

/***CommonInitBlock***/
    $actorSymbol(sum) = $val(init);
/**/

/***StringInitBlock***/
    $actorSymbol(sum) = strdup($val(init));
/**/

/***IntFireBlock***/
    $ref(output) = $actorSymbol(sum);
    $actorSymbol(state) += $val(step);
/**/

/***DoubleFireBlock***/
    $ref(output) = $actorSymbol(sum);
    $actorSymbol(state) += $val(step);
/**/

/***BooleanFireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) |= $val(step);
/**/

/***StringFireBlock***/
    $ref(output) = (char*) realloc($ref(output), sizeof(char) * (strlen($actorSymbol(state)) + 1) );
	strcpy($ref(output), $actorSymbol(state));
    $actorSymbol(state) = (char*) realloc($actorSymbol(state), sizeof(char) * (strlen($actorSymbol(state)) + strlen($val(step)) + 1) );
	strcat($actorSymbol(state),  $val(step));
/**/

/***TokenFireBlock***/
    $ref(output) = $actorSymbol(state);
	$actorSymbol(state) = $tokenFunc($ref(output)::add($val(step)));
/**/








/***initReset***/
    $actorSymbol(resetTemp) = $ref(reset#0);
/**/

/***readReset($arg)***/
    $actorSymbol(resetTemp) = $actorSymbol(resetTemp) || $ref(reset#$arg);
/**/

/***initSum***/
    if ($actorSymbol(resetTemp)) {
        $actorSymbol(sum) = $val(init);
    } 	
/**/

/***readInput($arg)***/
    $actorSymbol(sum) += $ref(input#$arg);
/**/

/***sendBlock***/
    $ref(output) = $actorSymbol(sum);
/**/
