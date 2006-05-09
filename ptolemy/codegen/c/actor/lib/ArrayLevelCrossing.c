/*** preinitBlock ***/
	int $actorSymbol(inputSize);
	int $actorSymbol(increment);
	double $actorSymbol(reference);
	double $actorSymbol(thresholdValue);
	int $actorSymbol(i);
/**/

/*** fireBlock ***/
        
    $actorSymbol(inputSize) = $ref(array).payload.Array->size();


    if (($ref(start) >= $actorSymbol(inputSize)) || ($ref(start) < 0)) {
    	// error;
    }

    $actorSymbol(increment) = -1;

    if ($ref(forwards)) {
        $actorSymbol(increment) = 1;
    }

    $actorSymbol(reference) = Array_get($ref(array), $ref(start)).payload.Double;

    $actorSymbol(thresholdValue) = $val(threshold);
/**/


=======================================================================
if ($ref(scale), "relative amplitude decibels")) {


    if ($val(above)) {
        $actorSymbol(thresholdValue) = $actorSymbol(reference)
                * Math.pow(10.0, ($actorSymbol(thresholdValue) / 20));
    } else {
        $actorSymbol(thresholdValue) = $actorSymbol(reference)
                * pow(10.0, (-$actorSymbol(thresholdValue) / 20));
    }

=======================================================================
        } else if ($ref(scale).equals("relative power decibels")) {


            if ($val(above)) {
                $actorSymbol(thresholdValue) = reference
                        * Math.pow(10.0, (thresholdValue / 10));
            } else {
                $actorSymbol(thresholdValue) = reference
                        * Math.pow(10.0, (-thresholdValue / 10));
            }
        } else if ($ref(scale).equals("relative linear")) {
            if ($val(above)) {
                thresholdValue = reference + thresholdValue;
            } else {
                thresholdValue = reference - thresholdValue;
            }
        }

        // Default output if we don't find a crossing.
        int bin = -1;

        for (int i = $ref(start); (i < inputSize) && (i >= 0); i += increment) {
            double currentValue = ((DoubleToken) $ref(array).getElement(i))
                    .doubleValue();

            if ($val(above)) {
                // Searching for values above the threshold.
                if (currentValue > thresholdValue) {
                    bin = i;
                    break;
                }
            } else {
                // Searching for values below the threshold.
                if (currentValue < thresholdValue) {
                    bin = i;
                    break;
                }
            }
        }

        $ref(output) = $actorSymbol(bin);
/**/

