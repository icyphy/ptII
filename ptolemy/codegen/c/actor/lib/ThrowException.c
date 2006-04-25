/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** fireBlock($channel) ***/
	if ($ref(input#$channel)) {
		fprintf(stderr, $val(message));
        exit(1);
    }
/**/

