// Gaussian has its own preinitBlock, so we include what is in the parent preinitBlock
/***preinitBlock***/
double $actorSymbol(mean);
long $actorSymbol(seed);
double $actorSymbol(standardDeviation);
Random $actorSymbol(_random);
/**/

/*** randomBlock ***/
$put(output, ($actorSymbol(_random).nextGaussian() * $param(standardDeviation)) + $param(mean));
/**/
