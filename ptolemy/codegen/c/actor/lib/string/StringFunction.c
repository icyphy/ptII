/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** toLowerCaseBlock ***/
	$ref(output) = (char*) malloc (sizeof(char) * (1 + strlen($ref(input))));
	for($actorSymbol(i) = 0; $ref(input)[ $actorSymbol(i) ]; $actorSymbol(i)++) {
    	$ref(output)[$actorSymbol(i)] = tolower($ref(input)[ $actorSymbol(i) ]);
    }
    $ref(output)[$actorSymbol(i)] = '\0';
/**/

/*** toUpperCaseBlock ***/
	$ref(output) = (char*) malloc (sizeof(char) * (1 + strlen($ref(input))));
	for($actorSymbol(i) = 0; $ref(input)[ $actorSymbol(i) ]; $actorSymbol(i)++) {
    	$ref(output)[$actorSymbol(i)] = toupper($ref(input)[ $actorSymbol(i) ]);
    }
    $ref(output)[$actorSymbol(i)] = '\0';
/**/

/*** trimBlock ***/
	$ref(output) = (char*) malloc (sizeof(char) * (1 + strlen($ref(input))));

	// FIXME: strtrim() is not a standard c function.
	strtrim($ref(output));
/**/
