/*** preinitBlock ***/
    double $actorSymbol(levels)[] = $val(levels);
    double* $actorSymbol(transitionPoints);
    int $actorSymbol(i);
/**/

//<numPoints> is number of transition points (# of levels - 1)
/*** initBlock (<numPoints>)***/
    // allocate space to store levels and transitionPoints
    // We use CALLOC to optimizate array performance
    $actorSymbol(transitionPoints) = (double*) calloc(<numPoints>, sizeof(double));
    
    for ($actorSymbol(i) = 0; $actorSymbol(i) < <numPoints>; $actorSymbol(i)++) {
        // transitionPoint[I] = (levels[I] + levels[I+1]) / 2;
        $actorSymbol(transitionPoints)[$actorSymbol(i)] = ($actorSymbol(levels)[$actorSymbol(i)] + $actorSymbol(levels)[$actorSymbol(i)]) / 2;
    }
/**/

/*** fireBlock (<numPoints>)***/
    $ref(output) = $actorSymbol(levels)[0];     // default
    for ($actorSymbol(i) = 0; $actorSymbol(i) < <numPoints>; $actorSymbol(i)++) {        
        if ($ref(input) > $actorSymbol(transitionPoints)[$actorSymbol(i)]) {
            $ref(output) = $actorSymbol(levels)[$actorSymbol(i) + 1];
        }
    }
/**/

/*** wrapupBlock ***/
    free($actorSymbol(transitionPoints));
/**/

