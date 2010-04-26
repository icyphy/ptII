/***preinitBlock***/
Binomial $actorSymbol(_generator);
$super();
/**/


/***binomialInitBlock***/
$actorSymbol(_generator) = new Binomial(1, 0.5, $actorSymbol(_randomNumberGenerator));
/**/

/*** binomialDistributionBlock ***/
if ($param(n) == 0) {
    $actorSymbol(current) = 0;
} else if ($param(p) == 0.0) {
    $actorSymbol(current) = 0;
} else if ($param(p) == 1.0) {
    $actorSymbol(current) = $param(n);
} else {
    $actorSymbol(current) = $actorSymbol(_generator).nextInt($param(n), $param(p));
}
$put(output, $actorSymbol(current))
/**/
