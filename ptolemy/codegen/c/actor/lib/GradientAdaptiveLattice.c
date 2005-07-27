/*** preinitBlock***/
    const int $actorSymbol(_order) = $size(reflectionCoefficients);

    double $actorSymbol(_backward)[$actorSymbol(_order)+1];
    double $actorSymbol(_backwardCache)[$actorSymbol(_order)+1];
    double $actorSymbol(_forward)[$actorSymbol(_order)+1];
    double $actorSymbol(_forwardCache)[$actorSymbol(_order)+1];
    double $actorSymbol(_reflectionCoefficients)[] = $val(reflectionCoefficients);
    double $actorSymbol(_estimatedErrorPower)[$actorSymbol(_order)+1];
    double $actorSymbol(_estimatedErrorPowerCache)[$actorSymbol(_order)+1];
    double $actorSymbol(_reflectionCoefficientsCache)[$actorSymbol(_order)];
    double $actorSymbol(_outputArray)[$actorSymbol(_order)];
    double $actorSymbol(outputArray)[$actorSymbol(_order)];
    
    int $actorSymbol(i);
    double $actorSymbol(k);
    double $actorSymbol(fe_i);
    double $actorSymbol(be_i);
    double $actorSymbol(fe_ip);
    double $actorSymbol(be_ip);
    double $actorSymbol(newError);
    double $actorSymbol(newCoefficient);
    double $actorSymbol(_oneMinusAlpha);
    double $actorSymbol(_alpha);
/**/

/*** initBlock ***/
    for ($actorSymbol(i) = 0; $actorSymbol(i) <= $actorSymbol(_order); $actorSymbol(i)++) {
        $actorSymbol(_forward)[$actorSymbol(i)] = 0;
        $actorSymbol(_backward)[$actorSymbol(i)] = 0;
        $actorSymbol(_estimatedErrorPower)[$actorSymbol(i)] = 0;
        $actorSymbol(_estimatedErrorPowerCache)[$actorSymbol(i)] = 0;
    }
    $actorSymbol(_oneMinusAlpha) = (($val(timeConstant) - 1.0) / ($val(timeConstant) + 1.0));
    $actorSymbol(_alpha) = 1.0 - $actorSymbol(_oneMinusAlpha);
/**/

/*** fireBlock ***/
    // System.arraycopy(_backward, 0, _backwardCache, 0, _order + 1);
    // System.arraycopy(_forward, 0, _forwardCache, 0, _order + 1);
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_order) + 1; $actorSymbol(i)++) {
        $actorSymbol(_backwardCache)[$actorSymbol(i)] = $actorSymbol(_backward)[$actorSymbol(i)];
        $actorSymbol(_forwardCache)[$actorSymbol(i)] = $actorSymbol(_forward)[$actorSymbol(i)];
    }

    $actorSymbol(_forwardCache)[0] = $ref(input); // _forwardCache(0) = x(n)

    // NOTE: The following code is ported from Ptolemy Classic.
    // Update forward errors.
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_order); $actorSymbol(i)++) {
        $actorSymbol(k) = $actorSymbol(_reflectionCoefficients)[$actorSymbol(i)];
        $actorSymbol(_forwardCache)[$actorSymbol(i) + 1] = (-$actorSymbol(k) * $actorSymbol(_backwardCache)[$actorSymbol(i)]) + $actorSymbol(_forwardCache)[$actorSymbol(i)];
    }


    // Backward: Compute the weights for the next round Note:
    // strictly speaking, _backwardCache[_order] is not necessary
    // for computing the output.  It is computed for the use of
    // subclasses which adapt the reflection coefficients.
    for ($actorSymbol(i) = $actorSymbol(_order); $actorSymbol(i) > 0; $actorSymbol(i)--) {
        $actorSymbol(k) = $actorSymbol(_reflectionCoefficients)[$actorSymbol(i) - 1];
        $actorSymbol(_backwardCache)[$actorSymbol(i)] = (-$actorSymbol(k) * $actorSymbol(_forwardCache)[$actorSymbol(i) - 1]) + $actorSymbol(_backwardCache)[$actorSymbol(i) - 1];

        $actorSymbol(fe_i) = $actorSymbol(_forwardCache)[$actorSymbol(i)];
        $actorSymbol(be_i) = $actorSymbol(_backwardCache)[$actorSymbol(i)];
        $actorSymbol(fe_ip) = $actorSymbol(_forwardCache)[$actorSymbol(i) - 1];
        $actorSymbol(be_ip) = $actorSymbol(_backwardCache)[$actorSymbol(i) - 1];

        $actorSymbol(newError) = ($actorSymbol(_estimatedErrorPower)[$actorSymbol(i)] * $actorSymbol(_oneMinusAlpha)) + ($actorSymbol(_alpha) * (($actorSymbol(fe_ip) * $actorSymbol(fe_ip)) + ($actorSymbol(be_ip) * $actorSymbol(be_ip))));
        $actorSymbol(newCoefficient) = $actorSymbol(_reflectionCoefficients)[$actorSymbol(i) - 1];

        if ($actorSymbol(newError) != 0.0) {
            $actorSymbol(newCoefficient) += (($actorSymbol(_alpha) * (($actorSymbol(fe_i) * $actorSymbol(be_ip)) + ($actorSymbol(be_i) * $actorSymbol(fe_ip)))) / $actorSymbol(newError));

            if ($actorSymbol(newCoefficient) > 1.0) {
                $actorSymbol(newCoefficient) = 1.0;
            } else if ($actorSymbol(newCoefficient) < -1.0) {
                $actorSymbol(newCoefficient) = -1.0;
            }
        }

        $actorSymbol(_outputArray)[$actorSymbol(i) - 1] = $actorSymbol(newCoefficient);
        $actorSymbol(_reflectionCoefficientsCache)[$actorSymbol(i) - 1] = $actorSymbol(newCoefficient);
        $actorSymbol(_estimatedErrorPowerCache)[$actorSymbol(i)] = $actorSymbol(newError);
    }

    //arraycopy(_outputArray, 0, outputArray, 0, _order);
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_order); $actorSymbol(i)++) {
        $actorSymbol(outputArray)[$actorSymbol(i)] = $actorSymbol(_outputArray)[$actorSymbol(i)];
    }
    $ref(adaptedReflectionCoefficients) = $actorSymbol(outputArray);

    $actorSymbol(_backwardCache)[0] = $actorSymbol(_forwardCache)[0]; // _backwardCache[0] = x[n]

    // Send the forward residual.
    $ref(output) = $actorSymbol(_forwardCache)[$actorSymbol(_order)];

    //arraycopy(_estimatedErrorPowerCache, 0, _estimatedErrorPower, 0, _order + 1);
    //arraycopy(_reflectionCoefficientsCache, 0, _reflectionCoefficients, 0, _order);
    //arraycopy(_backwardCache, 0, _backward, 0, _order + 1);
    //arraycopy(_forwardCache, 0, _forward, 0, _order + 1);
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_order) + 1; $actorSymbol(i)++) {
        $actorSymbol(_estimatedErrorPower)[$actorSymbol(i)] = $actorSymbol(_estimatedErrorPowerCache)[$actorSymbol(i)];
        $actorSymbol(_backward)[$actorSymbol(i)] = $actorSymbol(_backwardCache)[$actorSymbol(i)];
        $actorSymbol(_forward)[$actorSymbol(i)] = $actorSymbol(_forwardCache)[$actorSymbol(i)];
    }
    for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(_order); $actorSymbol(i)++) {
        $actorSymbol(_reflectionCoefficients)[$actorSymbol(i)] = $actorSymbol(_reflectionCoefficientsCache)[$actorSymbol(i)];
    }
/**/

