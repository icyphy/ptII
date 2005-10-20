/***initBlock***/
    $actorSymbol(state) = $val(init);
/**/

/***fireBlock***/
    $ref(output) = $actorSymbol(state);
    $actorSymbol(state) += $val(step);
/**/