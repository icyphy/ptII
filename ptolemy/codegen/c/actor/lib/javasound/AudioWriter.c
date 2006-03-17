/*** preinitBlock ***/
    FILE* $actorSymbol(filePtr);
/**/

/*** initBlock($fileName) ***/
    if (!($actorSymbol(filePtr) = fopen ($fileName,"w"))) {
        fprintf(stderr,"ERROR: cannot open file $fileName for AudioWriter actor.\n");
        exit(1);
    }
/**/

/*** writeSoundFile ***/

/**/

/*** wrapupBlock ***//**/

