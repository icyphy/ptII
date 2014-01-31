// Bernoulli has its own preinitBlock, so we include what is in the parent preinitBlock
/***preinitBlock***/
long $actorSymbol(seed);
Random $actorSymbol(_random);
/**/

/*** randomBlock ***/
if ($actorSymbol(_random).nextDouble() < $val(trueProbability)) {
    $put(output, true);
} else {
    $put(output, false);
}
/**/
