/***preinitBlock***/
Poisson $actorSymbol(_generator);
$super();
/**/

/***poissonInitBlock***/
$actorSymbol(_generator) = new Poisson(1.0, $actorSymbol(_randomNumberGenerator));
/**/

/*** poissonDistributionBlock ***/
$put(output, $actorSymbol(_generator).nextInt($get(mean)));
/**/
