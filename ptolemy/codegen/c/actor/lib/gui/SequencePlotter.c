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
/**/

/***writeFile***/
    // FIXME: Need to handle all channels in the multiport input.
    fprintf($actorSymbol(filePtr),"%d %g\n", $actorSymbol(count), $ref(input#0));
    $actorSymbol(count) ++;
/**/

/***closeFile***/
    fclose($actorSymbol(filePtr));
/**/

/***graphPlot***/
    // You might need to specify c:/.../ptII/bin/pxgraph below
    // in the final version for Ptolemy II, we use ptplot, not pxgraph
    //system("( pxgraph -t 'Butterfly' -bb -tk =600x600+0+0 -0 xy $actorSymbol(filename); /bin/rm -f $actorSymbol(filename)) &");
    system(" ptplot $actorSymbol(output) &"); // /bin/rm -f $actorSymbol(filename) &");
/**/

