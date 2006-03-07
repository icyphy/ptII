/***preinitBlock***/
    FILE* $actorSymbol(filePtr);
static int $actorSymbol(count) = 0;
/**/

/***initBlock***/
    if(!($actorSymbol(filePtr) = fopen("$actorSymbol(output)","w"))) {
        fprintf(stderr,"ERROR: cannot open output file for Plotter actor.\n");
        exit(1);
    }
    fprintf($actorSymbol(filePtr), "Grid: on\n");
    fprintf($actorSymbol(filePtr), "Impulses: on\n");
    fprintf($actorSymbol(filePtr), "Marks: dots\n");
    fprintf($actorSymbol(filePtr), "ReuseDataSets: on\n");
/**/

/***annotateBlock(<annotation>)***/
    fprintf($actorSymbol(filePtr), "<annotation>\n");
/**/

/***writeFile(<channel>)***/
    fprintf($actorSymbol(filePtr),"%d %g\n", $actorSymbol(count), $ref(input#<channel>));
/**/

/***countIncrease***/
    $actorSymbol(count) ++;
/**/

/***wrapupBlock***/
    fclose($actorSymbol(filePtr));
    system("ptplot $actorSymbol(output) &"); 
/**/


