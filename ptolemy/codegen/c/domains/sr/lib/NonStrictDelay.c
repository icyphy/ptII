/*** preinitBlock ***/
        $targetType(output) $actorSymbol(previousToken);
/**/

/*** initBlock ***/
        $actorSymbol(previousToken) = $val(initialValue);
/**/

/***fireBlock***/
        $ref(output) = $actorSymbol(previousToken);
/**/

/***postfireBlock***/
        $actorSymbol(previousToken) = $ref(input);
/**/
