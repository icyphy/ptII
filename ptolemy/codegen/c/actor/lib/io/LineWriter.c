/***preinitBlock***/
    boolean $actorSymbol(overwriteOK) = true;
    FILE * $actorSymbol(filePtr);
/**/
    
/***confirmOverwrite($fileName)***/
	do {
	    printf("OK to overwrite $fileName [1 = Yes, 0 = No]? ");
    	scanf ("%c", $actorSymbol(overwriteOK));
    } while ($actorSymbol(overwriteOK) != 1 && $actorSymbol(overwriteOK) != 0);
/**/

/***openForStdout***/
    $actorSymbol(filePtr) = stdout;
/**/
    
/***openForAppend($fileName)***/
    if (!($actorSymbol(filePtr) = fopen ($fileName,"a"))) {
        fprintf(stderr,"ERROR: cannot open output file for LineWriter actor.\n");
        exit(1);
    }
/**/

/***openForWrite($fileName)***/
    if (!($actorSymbol(filePtr) = fopen ($fileName,"w"))) {
        fprintf(stderr,"ERROR: cannot open output file for LineWriter actor.\n");
        exit(1);
    }
/**/

/***writeLine***/
	if ($actorSymbol(overwriteOK)) {
	    fprintf($actorSymbol(filePtr), "%s\n", $ref(input));
	}
/**/

/***wrapUpBlock***/
    fclose($actorSymbol(filePtr));
/**/
