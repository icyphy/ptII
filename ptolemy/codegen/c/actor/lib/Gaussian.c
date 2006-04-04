// The algorithm of generating the random number with Gaussian distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** preinitBlock ***/
    double $actorSymbol(seed);
    boolean $actorSymbol(haveNextNextGaussian) = false;
    double $actorSymbol(nextNextGaussian);

    // intermediate values
    double $actorSymbol(v1), $actorSymbol(v2), $actorSymbol(s);   
    double $actorSymbol(multiplier);
/**/

/*** randomBlock ***/
    if ($actorSymbol(haveNextNextGaussian)) {
        $actorSymbol(haveNextNextGaussian) = false;
        $ref(output) = $actorSymbol(nextNextGaussian);
    } else {
        do { 
            $actorSymbol(v1) = 2 * RandomSource_nextDouble(&$actorSymbol(seed)) - 1;   // between -1.0 and 1.0
            $actorSymbol(v2) = 2 * RandomSource_nextDouble(&$actorSymbol(seed)) - 1;   // between -1.0 and 1.0
            $actorSymbol(s) = $actorSymbol(v1) * $actorSymbol(v1) + $actorSymbol(v2) * $actorSymbol(v2);
        } while ($actorSymbol(s) >= 1 || $actorSymbol(s) == 0);

        $actorSymbol(multiplier) = sqrt(-2 * log($actorSymbol(s))/$actorSymbol(s));
        $actorSymbol(nextNextGaussian) = $actorSymbol(v2) * $actorSymbol(multiplier);
        $actorSymbol(haveNextNextGaussian) = true;
        $ref(output) = $actorSymbol(v1) * $actorSymbol(multiplier);
    }
    $ref(output) = ($ref(output) * $val(standardDeviation)) + $val(mean);    
/**/
