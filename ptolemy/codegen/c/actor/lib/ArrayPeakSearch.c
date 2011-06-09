/*** sharedBlock ***/
double $actorClass(indata);
double $actorClass(squelchValue);
int $actorClass(i);
double $actorClass(dipThreshold);
double $actorClass(riseThreshold);
int $actorClass(inputSize);
boolean $actorClass(searchValley);
boolean $actorClass(searchPeak);
int $actorClass(localMaxIndex);
double $actorClass(localMax);
double $actorClass(localMin);
int $actorClass(start);
int $actorClass(end);
int $actorClass(increment);
int $actorClass(resultIndex);
/**/

/*** initBlock ***/
$ref(peakValues) = $new(DoubleArray(0, 0));
$ref(peakIndices) = $new(IntArray(0, 0));
/**/


/*** fireBlock ($scale) ***/
    $actorClass(inputSize) = $size(input);

    if ($actorClass(inputSize) == 0) {
                $ref(peakValues) = $ref(input);
                $ref(peakIndices) = $convert_DoubleArray_IntArray($ref(input));
//        for ($actorClass(i) = 0; $actorClass(i) < $val(maximumNumberOfPeaks); $actorClass(i)++) {
//            DoubleArray_set($ref(peakValues), $actorClass(i), DoubleArray_get($ref(input), $actorClass(i)));
//            IntArray_set($ref(peakIndices), $actorClass(i), (int) DoubleArray_get($ref(input), $actorClass(i)));
//        }
    } else {
        $actorClass(start) = $ref(startIndex);
        $actorClass(end) = $ref(endIndex);

            // Constrain start and end.
            if ($actorClass(end) >= $actorClass(inputSize)) {
                $actorClass(end) = $actorClass(inputSize) - 1;
            }

            if ($actorClass(start) >= $actorClass(inputSize)) {
                $actorClass(start) = $actorClass(inputSize) - 1;
            }

            if ($actorClass(end) < 0) {
                $actorClass(end) = 0;
            }

            if ($actorClass(start) < 0) {
                $actorClass(start) = 0;
            }

            $actorClass(increment) = 1;

            if ($actorClass(end) < $actorClass(start)) {
                $actorClass(increment) = -1;
            }

            $actorClass(searchValley) = false;
            $actorClass(searchPeak) = true;

            $actorClass(localMaxIndex) = $actorClass(start);
            $actorClass(localMax) = DoubleArray_get($ref(input), $actorClass(start));
            $actorClass(localMin) = $actorClass(localMax);
            $actorClass(squelchValue) = $val(squelch);

            // The following values change since they are relative to
            // most recently peaks or values.
            $actorClass(dipThreshold) = $val(dip);
            $actorClass(riseThreshold) = $val(dip);

        $this.fireBlock1_$scale($scale)
        $this.fireBlock2($scale)

    }
/**/

/*** fireBlock1_ABSOLUTE($scale) ***/
/**/

/*** fireBlock1_RELATIVE_DB($scale) ***/
    $this.fireBlock1_ScaleNotAbsolute($scale)
/**/

/*** fireBlock1_RELATIVE_DB_POWER($scale) ***/
    $this.fireBlock1_ScaleNotAbsolute($scale)
/**/

/*** fireBlock1_RELATIVE_LINEAR($scale) ***/
    $this.fireBlock1_ScaleNotAbsolute($scale)
/**/


/*** fireBlock1_ScaleNotAbsolute($scale) ***/
    // Scale is relative so we adjust the thresholds.
    // Search for the global maximum value so squelch
    // works properly.
    double $actorClass(maxValue) = $actorClass(localMax);

    for ($actorClass(i) = 0; $actorClass(i) <= $actorClass(inputSize) - 1; $actorClass(i) += $actorClass(increment)) {
        $actorClass(indata) = DoubleArray_get($ref(input), $actorClass(i));

        if ($actorClass(indata) > $actorClass(maxValue)) {
            $actorClass(maxValue) = $actorClass(indata);
        }
    }
    $this.fireBlock11_$scale()
/**/


/*** fireBlock11_RELATIVE_DB ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 20));
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 20));
    $actorClass(squelchValue) = $actorClass(maxValue) * pow(10.0, (-$actorClass(squelchValue) / 20));
/**/


/*** fireBlock11_RELATIVE_DB_POWER ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 10));
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 10));
    $actorClass(squelchValue) = $actorClass(maxValue) * pow(10.0, (-$actorClass(squelchValue) / 10));
/**/


/*** fireBlock11_RELATIVE_LINEAR ***/
    $actorClass(dipThreshold) = $actorClass(localMax) - $val(dip);
    $actorClass(riseThreshold) = $actorClass(localMin) + $val(dip);
    $actorClass(squelchValue) = $actorClass(maxValue) - $actorClass(squelchValue);
/**/

/*** fireBlock2 ($scale) ***/
            for ($actorClass(i) = $actorClass(start); $actorClass(i) <= $actorClass(end); $actorClass(i) += $actorClass(increment)) {
                $actorClass(indata) = DoubleArray_get($ref(input), $actorClass(i));

                if ($actorClass(searchValley)) {
                    if ($actorClass(indata) < $actorClass(localMin)) {
                        $actorClass(localMin) = $actorClass(indata);

                    $this.fireBlock21_$scale()
                }

                if (($actorClass(indata) > $actorClass(riseThreshold)) && ($actorClass(indata) > $actorClass(squelchValue))) {
                    $actorClass(localMax) = $actorClass(indata);

                    $this.fireBlock22_$scale()

                    $actorClass(localMaxIndex) = $actorClass(i);
                    $actorClass(searchValley) = false;
                    $actorClass(searchPeak) = true;
                }
            } else if ($actorClass(searchPeak)) {
                if (($actorClass(indata) > $actorClass(localMax)) && ($actorClass(indata) > $actorClass(squelchValue))) {
                    $actorClass(localMax) = $actorClass(indata);

                    $this.fireBlock23_$scale()

                    $actorClass(localMaxIndex) = $actorClass(i);
                }

                if (($actorClass(indata) < $actorClass(dipThreshold)) && ($actorClass(localMax) > $actorClass(squelchValue))) {

                    DoubleArray_insert($ref(peakValues), $actorClass(localMax));
                    IntArray_insert($ref(peakIndices), $actorClass(localMaxIndex));

                    if ($ref(peakValues).payload.DoubleArray->size > $val(maximumNumberOfPeaks)) {
                        break;
                    }

                    $actorClass(localMin) = $actorClass(indata);

                    $this.fireBlock24_$scale()

                    $actorClass(searchValley) = true;
                    $actorClass(searchPeak) = false;
                }
            }
        }

        if ($ref(peakIndices).payload.IntArray->size == 0) {
            DoubleArray_insert($ref(peakValues), DoubleArray_get($ref(input), $actorClass(start)));
            IntArray_insert($ref(peakIndices), $ref(startIndex));
        }

/**/

/*** fireBlock21_ABSOLUTE ***/
/**/

/*** fireBlock21_RELATIVE_DB ***/
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 20));
/**/

/*** fireBlock21_RELATIVE_DB_POWER ***/
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 10));
/**/

/*** fireBlock21_RELATIVE_LINEAR ***/
    $actorClass(riseThreshold) = $actorClass(localMin) + $val(dip);
/**/


/*** fireBlock22_ABSOLUTE ***/
/**/

/*** fireBlock22_RELATIVE_DB ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 20));
/**/

/*** fireBlock22_RELATIVE_DB_POWER ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 10));
/**/

/*** fireBlock22_RELATIVE_LINEAR ***/
    $actorClass(dipThreshold) = $actorClass(localMax) - $val(dip);
/**/



/*** fireBlock23_ABSOLUTE ***/
/**/

/*** fireBlock23_RELATIVE_DB ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 20));
/**/

/*** fireBlock23_RELATIVE_DB_POWER ***/
    $actorClass(dipThreshold) = $actorClass(localMax) * pow(10.0, (-$val(dip) / 10));
/**/

/*** fireBlock23_RELATIVE_LINEAR ***/
    $actorClass(dipThreshold) = $actorClass(localMax) - $val(dip);
/**/


/*** fireBlock24_ABSOLUTE ***/
/**/

/*** fireBlock24_RELATIVE_DB ***/
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 20));
/**/

/*** fireBlock24_RELATIVE_DB_POWER ***/
    $actorClass(riseThreshold) = $actorClass(localMin) * pow(10.0, ($val(dip) / 10));
/**/

/*** fireBlock24_RELATIVE_LINEAR ***/
    $actorClass(riseThreshold) = $actorClass(localMin) + $val(dip);
/**/
