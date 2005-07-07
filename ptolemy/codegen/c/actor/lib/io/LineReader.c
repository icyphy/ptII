/***preinitBlock***/
    FILE * $actorSymbol(filePtr);
    int $actorSymbol(charRead);
    int $actorSymbol(index);
    int $actorSymbol(length) = 80;
/**/

/***initBlock***/
	$ref(output) = (char*) malloc($actorSymbol(length) * sizeof(char));
/**/
    

/***openForStdin***/
    $actorSymbol(filePtr) = stdin;
/**/
         
/***skipLine***/
    // use fgetc() to readLine
    //$actorSymbol(charReturned) = fscanf($actorSymbol(filePtr), "%s", $actorSymbol(line));
    while ( ($actorSymbol(charRead) = fgetc($actorSymbol(filePtr))) != '\n' && $actorSymbol(charRead) != EOF );
    $ref(endOfFile) = feof($actorSymbol(filePtr) );    
/**/

/***openForRead(<arg>)***/
    if (!($actorSymbol(filePtr) = fopen ("<arg>","r"))) {
        fprintf(stderr,"ERROR: cannot open file for LineReader actor.\n");
        exit(1);
    }
/**/

/***readLine***/
    //$actorSymbol(charReturned) = fscanf($actorSymbol(filePtr), "%s", $ref(output));
    $actorSymbol(index) = 0;
    do {
        $actorSymbol(charRead) = fgetc($actorSymbol(filePtr));
        if ($actorSymbol(index) >= $actorSymbol(length)) {
            $actorSymbol(length) *= 2;
            $ref(output) = (char*) realloc ($ref(output), $actorSymbol(length) * sizeof(char));
        }
        $ref(output)[$actorSymbol(index)++] = $actorSymbol(charRead);
    } while ( $actorSymbol(charRead) != '\n' && $actorSymbol(charRead) != EOF );
    $ref(endOfFile) = feof($actorSymbol(filePtr) );
/**/

/***wrapUpBlock***/
    fclose($actorSymbol(filePtr));
/**/
