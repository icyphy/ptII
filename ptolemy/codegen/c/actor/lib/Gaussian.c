// The algorithm of generating the random number with Gaussian distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** preinitBlock ***/
    double $actorSymbol(seed);
    boolean $actorSymbol(haveNextNextGaussian) = false;
    double $actorSymbol(nextNextGaussian);
/**/

/*** randomBlock ***/
    $ref(output) = (RandomSource_nextGaussian(&$actorSymbol(seed), &$actorSymbol(haveNextNextGaussian), &$actorSymbol(nextNextGaussian)) * $val(standardDeviation)) + $val(mean);
/**/
