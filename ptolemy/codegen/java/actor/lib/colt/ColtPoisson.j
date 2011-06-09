/***preinitBlock***/
Poisson $actorSymbol(_generator);
$super();
/**/

/***poissonInitBlock***/
$actorSymbol(_generator) = new Poisson(1.0, $actorSymbol(_randomNumberGenerator));
/**/

/*** poissonDistributionBlock ***/
$ref(output) = $actorSymbol(_generator).nextInt($ref(mean));
/**/
