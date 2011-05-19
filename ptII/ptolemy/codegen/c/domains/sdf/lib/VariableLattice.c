/*** preinitBlock ***/
    $super.preinitOrderBlock()
    double *$actorSymbol(_forward);
    double *$actorSymbol(_backward);
    double *$actorSymbol(_forwardCache);
    double *$actorSymbol(_backwardCache);
    double *$actorSymbol(_reflectionCoefs);
/**/

/*** initBlock ***/
    $this.reallocateBlock()
    $this.initReflectionCoefsBlock()
/**/

/*** sharedBlock ***/
$super()
int $actorClass(j);
int $actorClass(_valueLength);
/**/

/*** fireBlock ***/
//this.prefireBlock();

$actorClass(_valueLength) = $ref(reflectionCoefficients).payload.DoubleArray->size;

$ref(reflectionCoefficients) = $ref(newCoefficients);

if ($actorClass(_valueLength) != $ref(reflectionCoefficients).payload.DoubleArray->size) {
    $this.reallocateBlock()
}

$this.initReflectionCoefsBlock()

for ($actorClass(j) = 0; $actorClass(j) < $val(blockSize); $actorClass(j)++) {
    $super.fireBlock($actorClass(_valueLength), $actorClass(j))
}
//this.postfireBlock();
/**/


/*** reallocateBlock ***/
    $actorClass(_valueLength) = $ref(reflectionCoefficients).payload.DoubleArray->size;

    // Need to allocate or reallocate the arrays.
    free($actorSymbol(_backward));
    free($actorSymbol(_backwardCache));
    free($actorSymbol(_forward));
    free($actorSymbol(_forwardCache));

    $actorSymbol(_backward) = (double*) malloc(($actorClass(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_backwardCache) = (double*) malloc(($actorClass(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_forward) = (double*) malloc(($actorClass(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_forwardCache) = (double*) malloc(($actorClass(_valueLength) + 1) * sizeof(double));
    $actorSymbol(_reflectionCoefs) = (double*) malloc(($actorClass(_valueLength)) * sizeof(double));
/**/

/*** initReflectionCoefsBlock ***/
    for ($actorClass(i) = 0; $actorClass(i) < $actorClass(_valueLength); $actorClass(i)++) {
        $actorSymbol(_reflectionCoefs)[$actorClass(i)] = DoubleArray_get($ref(reflectionCoefficients), $actorClass(i));
    }
/**/
