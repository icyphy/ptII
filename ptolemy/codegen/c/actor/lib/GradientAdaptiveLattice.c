/*** preinitBlock ***/
/**/

/*** initBlock ***/
        _backward = new double[_order + 1];
        _backwardCache = new double[_order + 1];
        _forward = new double[_order + 1];
        _forwardCache = new double[_order + 1];
        _reflectionCoefficients = new double[_order];
        _estimatedErrorPower = new double[_order + 1];
        _estimatedErrorPowerCache = new double[_order + 1];
        _reflectionCoefficientsCache = new double[_order];
/**/

/*** fireBlock ***/
    if (input.hasToken(0)) {
        DoubleToken in = (DoubleToken) input.get(0);

        _forwardCache[0] = in.doubleValue(); // _forwardCache(0) = x(n)

        double k;

        // NOTE: The following code is ported from Ptolemy Classic.
        // Update forward errors.
        for (int i = 0; i < _order; i++) {
            k = _reflectionCoefficients[i];
            _forwardCache[i + 1] = (-k * _backwardCache[i]) + _forwardCache[i];
        }

        Token[] outputArray = new Token[_order];

        // Backward: Compute the weights for the next round Note:
        // strictly speaking, _backwardCache[_order] is not necessary
        // for computing the output.  It is computed for the use of
        // subclasses which adapt the reflection coefficients.
        for (int i = _order; i > 0; i--) {
            k = _reflectionCoefficients[i - 1];
            _backwardCache[i] = (-k * _forwardCache[i - 1])
                    + _backwardCache[i - 1];

            double fe_i = _forwardCache[i];
            double be_i = _backwardCache[i];
            double fe_ip = _forwardCache[i - 1];
            double be_ip = _backwardCache[i - 1];

            double newError = (_estimatedErrorPower[i] * _oneMinusAlpha)
                    + (_alpha * ((fe_ip * fe_ip) + (be_ip * be_ip)));
            double newCoefficient = _reflectionCoefficients[i - 1];

            if (newError != 0.0) {
                newCoefficient += ((_alpha * ((fe_i * be_ip) + (be_i * fe_ip))) / newError);

                if (newCoefficient > 1.0) {
                    newCoefficient = 1.0;
                } else if (newCoefficient < -1.0) {
                    newCoefficient = -1.0;
                }
            }

            outputArray[i - 1] = new DoubleToken(newCoefficient);
            _reflectionCoefficientsCache[i - 1] = newCoefficient;
            _estimatedErrorPowerCache[i] = newError;
        }

        adaptedReflectionCoefficients.send(0, new ArrayToken(outputArray));

        _backwardCache[0] = _forwardCache[0]; // _backwardCache[0] = x[n]

        // Send the forward residual.
        output.broadcast(new DoubleToken(_forwardCache[_order]));
    }
/**/

/*** wrapupBlock ***/
        System.arraycopy(_estimatedErrorPowerCache, 0, _estimatedErrorPower, 0, _order + 1);
        System.arraycopy(_reflectionCoefficientsCache, 0, _reflectionCoefficients, 0, _order);
        System.arraycopy(_backwardCache, 0, _backward, 0, _order + 1);
        System.arraycopy(_forwardCache, 0, _forward, 0, _order + 1);
/**/

