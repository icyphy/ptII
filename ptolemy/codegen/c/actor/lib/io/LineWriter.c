/***preinitBlock***/
    int $actorSymbol(overwriteOK) = 1;       // default: overwrite OK
    FILE * $actorSymbol(filePtr);
    int $actorSymbol(index);    
    int $actorSymbol(charToWrite);
/**/
    
/***confirmOverwrite***/
    printf("overwrite $val(fileName) [1 = Yes, 0 = No]? ");
    scanf ("%d",&$actorSymbol(overwriteOK));
/**/

/***openForStdout***/
    $actorSymbol(filePtr) = stdout;
/**/
    
/***openForAppend***/
    if (!($actorSymbol(filePtr) = fopen ($val(fileName),"a"))) {
        fprintf(stderr,"ERROR: cannot open output file for LineWriter actor.\n");
        exit(1);
    }
/**/

/***openForWrite***/
    if (!($actorSymbol(filePtr) = fopen ($val(fileName),"w"))) {
        fprintf(stderr,"ERROR: cannot open output file for LineWriter actor.\n");
        exit(1);
    }
/**/

/***writeLine***/
    $actorSymbol(index) = 0;
    while ( ($actorSymbol(charToWrite) = $ref(input)[$actorSymbol(index)++]) != '\n' ) { 
        fputc($actorSymbol(charToWrite), $actorSymbol(filePtr));
    } 
    fputc('\n', $actorSymbol(filePtr));
/**/

/***wrapUpBlock***/
    fclose($actorSymbol(filePtr));
/**/
