/*** preinitBlock ***/
    int $actorSymbol(i);
/**/

/*** printDouble(<channel>) ***/
    fprintf(stdout, "Display: %g\n", $ref(input#<channel>));
/**/

/*** printString(<channel>) ***/
    fprintf(stdout, "Display: %s\n", $ref(input#<channel>));
/**/

// FIXME: how do we handle different types??
/*** printArray(<channel>) ***/
    fprintf(stdout, "Display: {");
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $size(input); $actorSymbol(i)++) {
        if ($actorSymbol(i) == 0) {
            fprintf(stdout, "%g", $ref(input#<channel>)[$actorSymbol(i)]);
        }
        else {
            fprintf(stdout, ", %g", $ref(input#<channel>)[$actorSymbol(i)]);            
        }
    }
    fprintf(stdout, "}");
/**/

