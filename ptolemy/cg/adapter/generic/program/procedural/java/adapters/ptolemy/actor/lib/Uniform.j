// Uniform has its own preinitBlock, so we include what is in the parent preinitBlock
/***preinitBlock***/
long $actorSymbol(seed);
Random $actorSymbol(_random);
/**/

/*** randomBlock ***/
$put(output, $actorSymbol(_random).nextDouble() * ($param(upperBound) - $param(lowerBound)) + $param(lowerBound));
/**/
