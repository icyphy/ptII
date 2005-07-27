/*** preinitBlock ***/
    int $actorSymbol(i);
/**/

/*** printInt(<channel>) ***/
    fprintf(stdout, "Display: %d\n", $ref(input#<channel>));
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
    for ($actorSymbol(i) = 0; $actorSymbol(i) < 4; $actorSymbol(i)++) {
        if ($actorSymbol(i) == 0) {
            printf("%g", $ref(input#<channel>)[$actorSymbol(i)]);
            //fprintf(stdout, "%g", $ref(input#<channel>)[$actorSymbol(i)]);
        }
        else {
            fprintf(", %g", $ref(input#<channel>)[$actorSymbol(i)]);            
        }
    }
    fprintf(stdout, "}\n");
/**/

