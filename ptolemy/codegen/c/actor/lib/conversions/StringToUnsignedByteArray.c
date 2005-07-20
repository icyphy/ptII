/*** preinitBlock ***/
    int $actorSymbol(i);
    int $actorSymbol(length);
/**/

/*** fireBlock ***/
    $actorSymbol(length) = strlen($ref(input));
    $ref(output) = (char*) malloc($actorSymbol(length) * sizeof(char));
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
        $ref(output)[$actorSymbol(i)] = $ref(input)[$actorSymbol(i)];
    }
/**/
