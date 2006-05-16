/*** preinitBlock ***/
int $actorSymbol(inputSize);
boolean $actorSymbol(searchValley);
boolean $actorSymbol(searchPeak)
int $actorSymbol(localMaxIndex);
double $actorSymbol(localMax);
double $actorSymbol(localMin);
double $actorSymbol(squelchValue);
double $actorSymbol(dipThreshold);
double $actorSymbol(riseThreshold);

ArrayList resultIndices = new ArrayList();
ArrayList resultPeaks = new ArrayList();
/**/

/*** fireBlock ***/
    $actorSymbol(inputSize) = $ref(input).payload.Array->size;

    if ($actorSymbol(inputSize) == 0) {
        $ref(peakValues) = $ref(input);
        $ref(peakIndices) = $ref(input);
    } else {	
	    // Constrain start and end.
	    if ($val(end) >= $actorSymbol(inputSize)) {
	        $val(end) = $actorSymbol(inputSize) - 1;
	    }
	
	    if ($val(start) >= $actorSymbol(inputSize)) {
	        $val(start) = $actorSymbol(inputSize) - 1;
	    }
	
	    if ($val(end) < 0) {
	        $val(end) = 0;
	    }
	
	    if ($val(start) < 0) {
	        $val(start) = 0;
	    }
	
	    $actorSymbol(increment) = 1;
	
	    if ($val(end) < $val(start)) {
	        $actorSymbol(increment) = -1;
	    }
	
	    $actorSymbol(searchValley) = false;
	    $actorSymbol(searchPeak) = true;
	
	    $actorSymbol(localMaxIndex) = start;
	    $actorSymbol(localMax) = ((DoubleToken) $ref(input).getElement(start)).doubleValue();
	    $actorSymbol(localMin) = $actorSymbol(localMax);
	
	    $actorSymbol(squelchValue) = $val(squelch);
	
	    // The following values change since they are relative to
	    // most recently peaks or values.
	    $actorSymbol(dipThreshold) = $val(dip);
	    $actorSymbol(riseThreshold) = $val(dip);
	
	
	
	
	
	
	
	
	
	
	
	    String scaleValue = scale.stringValue();
	
	    // Index of what scale we are dealing with.
	    int scaleIndicator = _ABSOLUTE;
	
	    if (!scaleValue.equals("absolute")) {
	        // Scale is relative so we adjust the thresholds.
	        // Search for the global maximum value so squelch
	        // works properly.
	        double $actorSymbol(maxValue) = $actorSymbol(localMax);
	
	        for (int i = 0; i <= ($actorSymbol(inputSize) - 1); i = i + $actorSymbol(increment)) {
	            double $actorSymbol(indata) = ((DoubleToken) $ref(input).getElement(i)).doubleValue();
	
	            if ($actorSymbol(indata) > $actorSymbol(maxValue)) {
	                $actorSymbol(maxValue) = $actorSymbol(indata);
	            }
	        }
	
	        if (scaleValue.equals("relative amplitude decibels")) {
	            scaleIndicator = _RELATIVE_DB;
	            $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 20));
	            $actorSymbol(riseThreshold) = localMin * Math.pow(10.0, ($val(dip) / 20));
	            squelchValue = $actorSymbol(maxValue) * Math.pow(10.0, (-squelchValue / 20));
	        } else if (scaleValue.equals("relative power decibels")) {
	            scaleIndicator = _RELATIVE_DB_POWER;
	            $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 10));
	            $actorSymbol(riseThreshold) = localMin * Math.pow(10.0, ($val(dip) / 10));
	            squelchValue = $actorSymbol(maxValue) * Math.pow(10.0, (-squelchValue / 10));
	        } else if (scaleValue.equals("relative linear")) {
	            scaleIndicator = _RELATIVE_LINEAR;
	            $actorSymbol(dipThreshold) = $actorSymbol(localMax) - $val(dip);
	            $actorSymbol(riseThreshold) = localMin + $val(dip);
	            squelchValue = $actorSymbol(maxValue) - squelchValue;
	        }
	    }
	
	
	    for (int i = start; i <= end; i = i + $actorSymbol(increment)) {
	        double $actorSymbol(indata) = ((DoubleToken) $ref(input).getElement(i)).doubleValue();

	        if ($actorSymbol(searchValley)) {
	            if ($actorSymbol(indata) < localMin) {
	                localMin = $actorSymbol(indata);
	
	                switch (scaleIndicator) {
	                case _RELATIVE_DB:
	                    $actorSymbol(riseThreshold) = localMin
	                            * Math.pow(10.0, ($val(dip) / 20));
	                    break;
	
	                case _RELATIVE_DB_POWER:
	                    $actorSymbol(riseThreshold) = localMin
	                            * Math.pow(10.0, ($val(dip) / 10));
	                    break;
	
	                case _RELATIVE_LINEAR:
	                    $actorSymbol(riseThreshold) = localMin + $val(dip);
	                    break;
	                }
	            }
	
	            if (($actorSymbol(indata) > $actorSymbol(riseThreshold)) && ($actorSymbol(indata) > squelchValue)) {
	                $actorSymbol(localMax) = $actorSymbol(indata);
	
	                switch (scaleIndicator) {
	                case _RELATIVE_DB:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 20));
	                    break;
	
	                case _RELATIVE_DB_POWER:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 10));
	                    break;
	
	                case _RELATIVE_LINEAR:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) - $val(dip);
	                    break;
	                }
	
	                $actorSymbol(localMaxIndex) = i;
	                $actorSymbol(searchValley) = false;
	                $actorSymbol(searchPeak) = true;
	            }
	        } else if ($actorSymbol(searchPeak)) {
	            if (($actorSymbol(indata) > $actorSymbol(localMax)) && ($actorSymbol(indata) > squelchValue)) {
	                $actorSymbol(localMax) = $actorSymbol(indata);
	
	                switch (scaleIndicator) {
	                case _RELATIVE_DB:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 20));
	                    break;
	
	                case _RELATIVE_DB_POWER:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) * Math.pow(10.0, (-$val(dip) / 10));
	                    break;
	
	                case _RELATIVE_LINEAR:
	                    $actorSymbol(dipThreshold) = $actorSymbol(localMax) - $val(dip);
	                    break;
	                }
	
	                $actorSymbol(localMaxIndex) = i;
	            }
	
	            if (($actorSymbol(indata) < $actorSymbol(dipThreshold)) && ($actorSymbol(localMax) > squelchValue)) {
	
	                resultIndices.add(new IntToken($actorSymbol(localMaxIndex)));
	                resultPeaks.add(new DoubleToken($actorSymbol(localMax)));
	
	                if (resultPeaks.size() > $val(maximumNumberOfPeaks)) {
	                    break;
	                }
	
	                localMin = $actorSymbol(indata);
	
	                switch (scaleIndicator) {
	                case _RELATIVE_DB:
	                    $actorSymbol(riseThreshold) = localMin * Math.pow(10.0, ($val(dip) / 20));
	                    break;
	
	                case _RELATIVE_DB_POWER:
	                    $actorSymbol(riseThreshold) = localMin * Math.pow(10.0, ($val(dip) / 10));
	                    break;
	
	                case _RELATIVE_LINEAR:
	                    $actorSymbol(riseThreshold) = localMin + $val(dip);
	                    break;
	                }
	
	                $actorSymbol(searchValley) = true;
	                $actorSymbol(searchPeak) = false;
	            }
	        }
	    }
	
	    if (resultPeaks.isEmpty()) {
	        resultPeaks.add($ref(input).getElement(start));
	        resultIndices.add(startIndex.getToken());
	    }
	
	    Token[] resultPeaksArray = (Token[]) resultPeaks.toArray(new Token[resultPeaks.size()]);
	    Token[] resultIndicesArray = (Token[]) resultIndices.toArray(new Token[resultIndices.size()]);
	
	    peakValues.send(0, new ArrayToken(resultPeaksArray));
	    peakIndices.send(0, new ArrayToken(resultIndicesArray));
    }
/**/

