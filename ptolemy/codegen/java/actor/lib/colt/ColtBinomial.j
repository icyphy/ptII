/***preinitBlock***/
Binomial $actorSymbol(_generator);
$super();
/**/


/***binomialInitBlock***/
$actorSymbol(_generator) = new Binomial(1, 0.5, $actorSymbol(_randomNumberGenerator));
/**/

/*** binomialDistributionBlock ***/
if ($ref(n) == 0) {
    $actorSymbol(current) = 0;
} else if ($ref(p) == 0.0) {
    $actorSymbol(current) = 0;
} else if ($ref(p) == 1.0) {
    $actorSymbol(current) = $ref(n);
} else {
    $actorSymbol(current) = $actorSymbol(_generator).nextInt($ref(n), $ref(p));
}
$ref(output) = $actorSymbol(current);
/**/
