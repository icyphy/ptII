/*** preinitBlock ***/
	int $actorSymbol(length);
/**/

/*** initBlock ***/
	$ref(output) = NULL;
/**/

/*** fireBlock ***/
        $actorSymbol(length) = $ref(stop) - $ref(start);
        /* Realloc space for the null */
$ref(output) = (char*) realloc($ref(output), $actorSymbol(length) + 1);
	$ref(output) = strncpy($ref(output), $ref(input) + $ref(start), $actorSymbol(length));
        /* Solaris: strncpy does not add a null if only n chars are copied. */ 
        $ref(output)[$actorSymbol(length)] = '\0';
/**/

