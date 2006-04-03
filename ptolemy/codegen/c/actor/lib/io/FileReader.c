/***preinitBlock***/
    FILE * $actorSymbol(filePtr);
    int $actorSymbol(charRead);
    int $actorSymbol(index);
    int $actorSymbol(length) = 80;
/**/

/***initBlock***/
	$ref(output) = (char*) malloc($actorSymbol(length) * sizeof(char));
/**/

/***openForRead($fileName)***/
    if (!($actorSymbol(filePtr) = fopen ("$fileName","r"))) {
        fprintf(stderr,"ERROR: cannot open file \"$fileName\" for LineReader actor.\n");
        exit(1);
    }
/**/

/***fireBlock***/
    $actorSymbol(index) = 0;
    do {
        $actorSymbol(charRead) = fgetc($actorSymbol(filePtr));
        if ($actorSymbol(index) >= $actorSymbol(length)) {
            $actorSymbol(length) *= 2;
            $ref(output) = (char*) realloc ($ref(output), ($actorSymbol(length) + 1) * sizeof(char));
            /* Solaris: strncpy does not add a null if only */
            /* n chars are copied. */ 
            $ref(output)[$actorSymbol(length)] = '\0';
        }
        if ($actorSymbol(charRead) != EOF ) {
            if ($actorSymbol(charRead) != '\r') {
                $ref(output)[$actorSymbol(index)++] = $actorSymbol(charRead);
            } else {
                fprintf(stderr, "Warning: dropping a \\r char\n");
            }
        } else {
            $ref(output)[$actorSymbol(index)++] = '\n';
        }
    } while ( $actorSymbol(charRead) != EOF );
/**/



/***wrapupBlock***/
    fclose($actorSymbol(filePtr));
/**/
