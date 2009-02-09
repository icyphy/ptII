// Uniform has its own preinitBlock, so we include what is in the parent preinitBlock
/***preinitBlock***/
long $actorSymbol(seed);
Random $actorSymbol(_random);
/**/

/*** randomBlock ***/
$ref(output) = $actorSymbol(_random).nextDouble() * ($ref(upperBound) - $ref(lowerBound)) + $ref(lowerBound);
/**/
