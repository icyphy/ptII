/***initBlock***/
    $actorSymbol(sum) = $val(init);
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
