/*** preinitBlock ***/
    $this.preinitOrderBlock()
    double $actorSymbol(_forward)[$actorSymbol(_order) + 1];
    double $actorSymbol(_backward)[$actorSymbol(_order) + 1];
    double $actorSymbol(_forwardCache)[$actorSymbol(_order) + 1];
    double $actorSymbol(_backwardCache)[$actorSymbol(_order) + 1];
    double $actorSymbol(_reflectionCoefs)[$actorSymbol(_order)];
/**/

/*** preinitOrderBlock ***/
    #define $actorSymbol(_order) $size(reflectionCoefficients)
/**/

/*** sharedBlock ***/
    double $actorClass(k);
    int $actorClass(i);
/**/

/*** sharedArrayCopyBlock ***/
    $super.arraycopyBlock(double)
/**/


/*** prefireBlock ***/
    //System.arraycopy(_backward, 0, _backwardCache, 0, _backwardCache.length);
    //System.arraycopy(_forward, 0, _forwardCache, 0, _forwardCache.length);
    double_arraycopy($actorSymbol(_backward), 0, $actorSymbol(_backwardCache), 0, $actorSymbol(_order) + 1);
    double_arraycopy($actorSymbol(_forward), 0, $actorSymbol(_forwardCache), 0, $actorSymbol(_order) + 1);
/**/

/*** postfireBlock ***/
    //System.arraycopy(_backwardCache, 0, _backward, 0, _backwardCache.length);
    //System.arraycopy(_forwardCache, 0, _forward, 0, _forwardCache.length);
    double_arraycopy($actorSymbol(_backwardCache), 0, $actorSymbol(_backward), 0, $actorSymbol(_order) + 1);
    double_arraycopy($actorSymbol(_forwardCache), 0, $actorSymbol(_forward), 0, $actorSymbol(_order) + 1);
/**/

/*** fireBlock ($coefLength, $bufferIndex) ***/
    //this.prefireBlock();
    $actorSymbol(_forwardCache)[0] = $ref(input, $bufferIndex); // $actorSymbol(_forwardCache)(0) = x(n)

    for ($actorClass(i) = 1; $actorClass(i) <= $coefLength; $actorClass(i)++) {
        $actorClass(k) = $actorSymbol(_reflectionCoefs)[$coefLength - $actorClass(i)];
        $actorSymbol(_forwardCache)[$actorClass(i)] = ($actorClass(k) * $actorSymbol(_backwardCache)[$actorClass(i)]) + $actorSymbol(_forwardCache)[$actorClass(i) - 1];
    }

    $ref(output, $bufferIndex) = $actorSymbol(_forwardCache)[$coefLength];

    for ($actorClass(i) = 1; $actorClass(i) < $coefLength; $actorClass(i)++) {
        $actorClass(k) = -$actorSymbol(_reflectionCoefs)[$coefLength - 1 - $actorClass(i)];
        $actorSymbol(_backwardCache)[$actorClass(i)] = $actorSymbol(_backwardCache)[$actorClass(i) + 1] + ($actorClass(k) * $actorSymbol(_forwardCache)[$actorClass(i) + 1]);
    }

    $actorSymbol(_backwardCache)[$coefLength] = $actorSymbol(_forwardCache)[$coefLength];
    //this.postfireBlock();
/**/

/*** fireBlock ***/
$this.fireBlock($actorSymbol(_order), 0)
/**/

/*** initReflectionCoefsBlock ***/
    //reallocate();

    for ($actorClass(i) = 0; $actorClass(i) < $actorSymbol(_order); $actorClass(i)++) {
        $actorSymbol(_reflectionCoefs)[$actorClass(i)] = DoubleArray_get($ref(reflectionCoefficients), $actorClass(i));
    }
/**/

/*** initBlock ***/
    $this.initReflectionCoefsBlock()
    for ($actorClass(i) = 0; $actorClass(i) < ($actorSymbol(_order) + 1); $actorClass(i)++) {
        $actorSymbol(_forward)[$actorClass(i)] = 0.0;
        $actorSymbol(_backward)[$actorClass(i)] = 0.0;
        $actorSymbol(_forwardCache)[$actorClass(i)] = 0.0;
        $actorSymbol(_backwardCache)[$actorClass(i)] = 0.0;
    }
/**/
