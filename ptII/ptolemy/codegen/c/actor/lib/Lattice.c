/*** preinitBlock ***/
    #define $actorSymbol(_order) $size(reflectionCoefficients)

    double $actorSymbol(_forward)[$actorSymbol(_order) + 1];
    double $actorSymbol(_backward)[$actorSymbol(_order) + 1];
    double $actorSymbol(_forwardCache)[$actorSymbol(_order) + 1];
    double $actorSymbol(_backwardCache)[$actorSymbol(_order) + 1];
    double $actorSymbol(_reflectionCoefficients)[$actorSymbol(_order)];
/**/

/*** sharedBlock ***/
    double $actorClass(k);
    int $actorClass(i);
/**/

/*** sharedArrayCopyBlock ***/
    //super.arraycopyBlock(double);
void $actorClass(arraycopy)(double *src, int srcPos, double *dest, int destPos, int length) {
    int i;
    for (i = 0; i < length; i++) {
        dest[destPos + i] = src[srcPos + i];
    }
}
/**/



/*** prefireBlock ***/
    $actorClass(arraycopy)($actorSymbol(_backward), 0, $actorSymbol(_backwardCache), 0, $actorSymbol(_order) + 1);
    $actorClass(arraycopy)($actorSymbol(_forward), 0, $actorSymbol(_forwardCache), 0, $actorSymbol(_order) + 1);
/**/

/*** postfireBlock ***/
    $actorClass(arraycopy)($actorSymbol(_backwardCache), 0, $actorSymbol(_backward), 0, $actorSymbol(_order) + 1);
    $actorClass(arraycopy)($actorSymbol(_forwardCache), 0, $actorSymbol(_forward), 0, $actorSymbol(_order) + 1);
/**/


/*** doFilterBlock ***/
    // NOTE: The following code is ported from Ptolemy Classic.
    // Update forward errors.
    for ($actorClass(i) = 0; $actorClass(i) < $actorSymbol(_order); $actorClass(i)++) {
        $actorClass(k) = $actorSymbol(_reflectionCoefficients)[$actorClass(i)];
        $actorSymbol(_forwardCache)[$actorClass(i) + 1] = (-$actorClass(k) * $actorSymbol(_backwardCache)[$actorClass(i)]) + $actorSymbol(_forwardCache)[$actorClass(i)];
    }

    // Backward: Compute the weights for the next round Note:
    // strictly speaking, _backwardCache[_order)] is not necessary
    // for computing the output.  It is computed for the use of
    // subclasses which adapt the reflection coefficients.
    for ($actorClass(i) = $actorSymbol(_order); $actorClass(i) > 0; $actorClass(i)--) {
        $actorClass(k) = $actorSymbol(_reflectionCoefficients)[$actorClass(i) - 1];
        $actorSymbol(_backwardCache)[$actorClass(i)] = (-$actorClass(k) * $actorSymbol(_forwardCache)[$actorClass(i) - 1]) + $actorSymbol(_backwardCache)[$actorClass(i) - 1];
    }
/**/

/*** fireBlock ***/
    $actorSymbol(_forwardCache)[0] = $ref(input); // $actorSymbol(_forwardCache)(0) = x(n)

    $this.doFilterBlock()

    $actorSymbol(_backwardCache)[0] = $actorSymbol(_forwardCache)[0]; // $actorSymbol(_backwardCache)[0] = x[n]

    // Send the forward residual.
    $ref(output) = $actorSymbol(_forwardCache)[$actorSymbol(_order)];
/**/

/*** initBlock ***/
    //reallocate();

    for ($actorClass(i) = 0; $actorClass(i) < $actorSymbol(_order); $actorClass(i)++) {
        $actorSymbol(_reflectionCoefficients)[$actorClass(i)] = DoubleArray_get($ref(reflectionCoefficients), $actorClass(i));
    }

    for ($actorClass(i) = 0; $actorClass(i) < ($actorSymbol(_order) + 1); $actorClass(i)++) {
        $actorSymbol(_forward)[$actorClass(i)] = 0.0;
        $actorSymbol(_backward)[$actorClass(i)] = 0.0;
        $actorSymbol(_forwardCache)[$actorClass(i)] = 0.0;
        $actorSymbol(_backwardCache)[$actorClass(i)] = 0.0;
    }
/**/
