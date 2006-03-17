/*** preinitBlock ***/
    FILE* $actorSymbol(filePtr);
/**/

/*** initBlock ***/    
    if (!($actorSymbol(filePtr) = fopen ($ref(fileOrURL),"w"))) {
        fprintf(stderr,"ERROR: cannot open file \"%s\" for AudioWriter actor.\n", $ref(fileOrURL));
        exit(1);
    }
/**/

/*** writeSoundFile ***/

/**/

/*** wrapupBlock ***//**/

