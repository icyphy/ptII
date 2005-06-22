/*** preinitBlock ***/
    FILE* $actorSymbol(filePtr);
/**/

/*** initBlock ***/    
    if (!($actorSymbol(filePtr) = fopen ($ref(fileOrURL),"w"))) {
        fprintf(stderr,"ERROR: cannot open file for AudioReader actor.\n");
        exit(1);
    }
/**/

/*** writeSoundFile ***/

/**/

/*** wrapupBlock ***/
/**/

