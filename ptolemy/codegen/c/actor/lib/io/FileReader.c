/***preinitBlock***/
    FILE * $actorSymbol(filePtr);

/**/

/***openForRead($fileName)***/
    if (!($actorSymbol(filePtr) = fopen ("$fileName","r"))) {
        fprintf(stderr,"ERROR: cannot open file \"$fileName\" for LineReader actor.\n");
        exit(1);
    }
/**/
