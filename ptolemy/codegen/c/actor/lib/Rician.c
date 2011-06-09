/*** preinitBlock ***/
$super()
boolean $actorSymbol(haveNextNextGaussian) = false;
double $actorSymbol(nextNextGaussian);

double $actorSymbol(xRawNum);
double $actorSymbol(yRawNum);
/**/

/*** randomBlock ***/
$actorSymbol(xRawNum) = RandomSource_nextGaussian(&$actorSymbol(seed), &$actorSymbol(haveNextNextGaussian), &$actorSymbol(nextNextGaussian));
$actorSymbol(yRawNum) = RandomSource_nextGaussian(&$actorSymbol(seed), &$actorSymbol(haveNextNextGaussian), &$actorSymbol(nextNextGaussian));
$ref(output) = sqrt(pow(
                            ($actorSymbol(xRawNum) * $val(standardDeviation)) + $val(xMean), 2)
        + pow(($actorSymbol(yRawNum) * $val(standardDeviation)) + $val(yMean), 2));
/**/

