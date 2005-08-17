/***preinitBlock***/
    FILE* $actorSymbol(filePtr);
/**/

/***initBlock***/
    if(!($actorSymbol(filePtr) = fopen("XYPlotter_tmpfile","w"))) {
        fprintf(stderr,"ERROR: cannot open output file for Plotter actor.\n");
        exit(1);
    }
/**/

/***writeFile***/
    fprintf($actorSymbol(filePtr),"%g %g\n",$ref(inputX#0),$ref(inputY#0));
/**/

/***closeFile***/
    fclose($actorSymbol(filePtr));
/**/

/***graphPlot***/
    // You might need to specify c:/.../ptII/bin/pxgraph below
    // in the final version for Ptolemy II, we use ptplot, not pxgraph
    //system("( pxgraph -t 'Butterfly' -bb -tk =600x600+0+0 -0 xy $actorSymbol(filename); /bin/rm -f $actorSymbol(filename)) &");
    system(" ptplot XYPlotter_tmpfile &"); // /bin/rm -f $actorSymbol(filename) &");
/**/

