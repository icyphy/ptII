/*** sharedBlock ***/
$super()
double $actorClass(fe_i);
double $actorClass(be_i);
double $actorClass(fe_ip);
double $actorClass(be_ip);
double $actorClass(newError);
double $actorClass(newCoefficient);
/**/


/*** preinitBlock***/
$super()
double $actorSymbol(_estimatedErrorPower)[$actorSymbol(_order)+1];
double $actorSymbol(_estimatedErrorPowerCache)[$actorSymbol(_order)+1];
double $actorSymbol(_reflectionCoefficientsCache)[$actorSymbol(_order)];

double $actorSymbol(_oneMinusAlpha);
double $actorSymbol(_alpha);
/**/

/*** initBlock ***/
    $super();
    $ref(adaptedReflectionCoefficients) = $new(DoubleArray($actorSymbol(_order), 0));

    for ($actorClass(i) = 0; $actorClass(i) <= $actorSymbol(_order); $actorClass(i)++) {
        $actorSymbol(_estimatedErrorPower)[$actorClass(i)] = 0;
        $actorSymbol(_estimatedErrorPowerCache)[$actorClass(i)] = 0;
    }
    $actorSymbol(_oneMinusAlpha) = (($val(timeConstant) - 1.0) / ($val(timeConstant) + 1.0));
    $actorSymbol(_alpha) = 1.0 - $actorSymbol(_oneMinusAlpha);
/**/

/*** doFilterBlock ***/
    // NOTE: The following code is ported from Ptolemy Classic.
    // Update forward errors.
    for ($actorClass(i) = 0; $actorClass(i) < $actorSymbol(_order); $actorClass(i)++) {
        $actorClass(k) = $actorSymbol(_reflectionCoefficients)[$actorClass(i)];
        $actorSymbol(_forwardCache)[$actorClass(i) + 1] = (-$actorClass(k) * $actorSymbol(_backwardCache)[$actorClass(i)]) + $actorSymbol(_forwardCache)[$actorClass(i)];
    }

    // Backward: Compute the weights for the next round Note:
    // strictly speaking, _backwardCache[_order] is not necessary
    // for computing the output.  It is computed for the use of
    // subclasses which adapt the reflection coefficients.
    for ($actorClass(i) = $actorSymbol(_order); $actorClass(i) > 0; $actorClass(i)--) {
        $actorClass(k) = $actorSymbol(_reflectionCoefficients)[$actorClass(i) - 1];
        $actorSymbol(_backwardCache)[$actorClass(i)] = (-$actorClass(k) * $actorSymbol(_forwardCache)[$actorClass(i) - 1]) + $actorSymbol(_backwardCache)[$actorClass(i) - 1];

        $actorClass(fe_i) = $actorSymbol(_forwardCache)[$actorClass(i)];
        $actorClass(be_i) = $actorSymbol(_backwardCache)[$actorClass(i)];
        $actorClass(fe_ip) = $actorSymbol(_forwardCache)[$actorClass(i) - 1];
        $actorClass(be_ip) = $actorSymbol(_backwardCache)[$actorClass(i) - 1];

        $actorClass(newError) = ($actorSymbol(_estimatedErrorPower)[$actorClass(i)] * $actorSymbol(_oneMinusAlpha)) + ($actorSymbol(_alpha) * (($actorClass(fe_ip) * $actorClass(fe_ip)) + ($actorClass(be_ip) * $actorClass(be_ip))));
        $actorClass(newCoefficient) = $actorSymbol(_reflectionCoefficients)[$actorClass(i) - 1];

        if ($actorClass(newError) != 0.0) {
            $actorClass(newCoefficient) += (($actorSymbol(_alpha) * (($actorClass(fe_i) * $actorClass(be_ip)) + ($actorClass(be_i) * $actorClass(fe_ip)))) / $actorClass(newError));

            if ($actorClass(newCoefficient) > 1.0) {
                $actorClass(newCoefficient) = 1.0;
            } else if ($actorClass(newCoefficient) < -1.0) {
                $actorClass(newCoefficient) = -1.0;
            }
        }

        $ref(adaptedReflectionCoefficients).payload.DoubleArray->elements[$actorClass(i) - 1] = $actorClass(newCoefficient);
        $actorSymbol(_reflectionCoefficientsCache)[$actorClass(i) - 1] = $actorClass(newCoefficient);
        $actorSymbol(_estimatedErrorPowerCache)[$actorClass(i)] = $actorClass(newError);
    }
/**/

/*** fireBlock ***/
    $this.prefireBlock()
    $super.fireBlock()
    $this.postfireBlock()
/**/

/*** postfireBlock ***/
    $super();
    $actorClass(arraycopy)($actorSymbol(_estimatedErrorPowerCache), 0, $actorSymbol(_estimatedErrorPower), 0, $actorSymbol(_order) + 1);
    $actorClass(arraycopy)($actorSymbol(_reflectionCoefficientsCache), 0, $actorSymbol(_reflectionCoefficients), 0, $actorSymbol(_order));
/**/

