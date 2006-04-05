/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** fireBlock ***/
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(input); $actorSymbol(i)++) {
	if ($ref(input#$actorSymbol(i))) {
            exit(0);
        }
    }
/**/

