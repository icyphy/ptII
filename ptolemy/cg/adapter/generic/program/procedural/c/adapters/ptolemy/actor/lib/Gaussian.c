// The algorithm of generating the random number with Gaussian distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** declareShared ***/
double RandomSource_nextGaussian(double* seed, boolean* haveNextNextGaussian, double* nextNextGaussian);
/**/

/*** preinitBlock ***/
$super()
double $actorSymbol(nextNextGaussian);
boolean $actorSymbol(haveNextNextGaussian) = false;
double $actorSymbol(nextNextGaussian);
/**/

/*** randomBlock ***/
$put(output, (RandomSource_nextGaussian(&$actorSymbol(seed), &$actorSymbol(haveNextNextGaussian), &$actorSymbol(nextNextGaussian))  * $param(standardDeviation)) + $param(mean));
/**/
