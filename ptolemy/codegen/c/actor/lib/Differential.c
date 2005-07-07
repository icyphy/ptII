/***preinitBlock***/
    double $actorSymbol(previousInput) = 0;
/**/

/***fireBlock***/
    $ref(output) = $ref(input) - $actorSymbol(previousInput);
/**/
