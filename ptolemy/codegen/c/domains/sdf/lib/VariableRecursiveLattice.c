/*** preinitBlock ***/
    int $actorSymbol(i);
    int $actorSymbol(M);
    double $actorSymbol(k);
    int $actorSymbol(_valueLength);
    double* $actorSymbol(_backward);
    double* $actorSymbol(_backwardCache);
    double* $actorSymbol(_forward);
    double* $actorSymbol(_forwardCache);
    double* $actorSymbol(_reflectionCoefs);
    int $actorSymbol(_blockSizeValue);
    double* $actorSymbol(coefficientsToken);
/**/

/*** initBlock ***/
    $actorSymbol(_valueLength) = $size(reflectionCoefficients);
    $actorSymbol(_backward) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_backwardCache) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_forward) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_forwardCache) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_reflectionCoefs) = (double*) malloc($actorSymbol(_valueLength) * sizeof(double));
    $actorSymbol(_blockSizeValue) = $val(blockSize);

    if ($actorSymbol(_blockSizeValue) < 1) {
        fprintf(stderr, "Invalid blockSize: %d,", $actorSymbol(_blockSizeValue));
        exit(1);
    }

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_valueLength); $actorSymbol(i)++) {
        $actorSymbol(_reflectionCoefs)[$actorSymbol(i)] = $ref(reflectionCoefficients)[$actorSymbol(i)];
    }

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_valueLength) + 1; $actorSymbol(i)++) {
        $actorSymbol(_forward)[$actorSymbol(i)] = 0;
        $actorSymbol(_backward)[$actorSymbol(i)] = 0;
    }
/**/

/*** fireBlock ***/
    // System.arraycopy(_backward, 0, _backwardCache, 0, _valueLength + 1);
    // System.arraycopy(_forward, 0, _forwardCache, 0, _valueLength + 1);
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_valueLength) + 1; $actorSymbol(i)++) {
        $actorSymbol(_backwardCache)[$actorSymbol(i)] = $actorSymbol(_backward)[$actorSymbol(i)];
        $actorSymbol(_forwardCache)[$actorSymbol(i)] = $actorSymbol(_forward)[$actorSymbol(i)];
    }

    // FIXME: we need to check if (newCoefficients.hasToken(0)) in the future
    $actorSymbol(coefficientsToken) = $ref(newCoefficients);
    $ref(reflectionCoefficients) = $actorSymbol(coefficientsToken);
   
   //--------------- reallocate -------------------------
    //if ((_backward == null) || (valueLength != (_backward.length - 1))) {
    if ($actorSymbol(_valueLength) != $size(reflectionCoefficients)) {
        $actorSymbol(_valueLength) = $size(reflectionCoefficients);
        
        // Need to allocate or reallocate the arrays.
        free($actorSymbol(_backward));
        free($actorSymbol(_backwardCache));
        free($actorSymbol(_forward));
        free($actorSymbol(_forwardCache));
        free($actorSymbol(_reflectionCoefs));
        
        $actorSymbol(_backward) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
        $actorSymbol(_backwardCache) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
        $actorSymbol(_forward) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
        $actorSymbol(_forwardCache) = (double*) malloc(($actorSymbol(_valueLength) + 1) * sizeof(double));
        $actorSymbol(_reflectionCoefs) = (double*) malloc($actorSymbol(_valueLength) * sizeof(double));
    }

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_valueLength); $actorSymbol(i)++) {
        $actorSymbol(_reflectionCoefs)[$actorSymbol(i)] = $ref(reflectionCoefficients)[$actorSymbol(i)];
    }
    //----------------------------------------------------   

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_blockSizeValue); $actorSymbol(i)++) {
        // NOTE: The following code is ported from Ptolemy Classic.
        $actorSymbol(M) = $actorSymbol(_valueLength);

        // Forward prediction error
        $actorSymbol(_forwardCache)[0] = $ref(input); // _forward(0) = x(n)

        for ($actorSymbol(i) = 1; $actorSymbol(i) <= $actorSymbol(M); $actorSymbol(i)++) {
            $actorSymbol(k) = $actorSymbol(_reflectionCoefs)[$actorSymbol(M) - $actorSymbol(i)];
            $actorSymbol(_forwardCache)[$actorSymbol(i)] = ($actorSymbol(k) * $actorSymbol(_backwardCache)[$actorSymbol(i)]) + $actorSymbol(_forwardCache)[$actorSymbol(i) - 1];
        }

        $ref(output) = $actorSymbol(_forwardCache)[$actorSymbol(M)];

        // Backward:  Compute the w's for the next round
        for ($actorSymbol(i) = 1; $actorSymbol(i) < $actorSymbol(M); $actorSymbol(i)++) {
            $actorSymbol(k) = -$actorSymbol(_reflectionCoefs)[$actorSymbol(M) - 1 - $actorSymbol(i)];
            $actorSymbol(_backwardCache)[$actorSymbol(i)] = $actorSymbol(_backwardCache)[$actorSymbol(i) + 1] + ($actorSymbol(k) * $actorSymbol(_forwardCache)[$actorSymbol(i) + 1]);
        }

        $actorSymbol(_backwardCache)[$actorSymbol(M)] = $actorSymbol(_forwardCache)[$actorSymbol(M)];
    }

    //arraycopy(_backwardCache, 0, _backward, 0, _valueLength + 1);
    //arraycopy(_forwardCache, 0, _forward, 0, _valueLength + 1);
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_valueLength) + 1; $actorSymbol(i)++) {
        $actorSymbol(_backward)[$actorSymbol(i)] = $actorSymbol(_backwardCache)[$actorSymbol(i)];
        $actorSymbol(_forward)[$actorSymbol(i)] = $actorSymbol(_forwardCache)[$actorSymbol(i)];
    }
/**/

/*** wrapupBlock ***/
/**/

