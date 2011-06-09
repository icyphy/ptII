/***preinitBlock***/
Binomial $actorSymbol(_generator);
$super();
/**/


/***binomialInitBlock***/
$actorSymbol(_generator) = new Binomial(1, 0.5, $actorSymbol(_randomNumberGenerator));
/**/

/*** binomialDistributionBlock ***/
if ($get(n) == 0) {
    $actorSymbol(current) = 0;
} else if ($get(p) == 0.0) {
    $actorSymbol(current) = 0;
} else if ($get(p) == 1.0) {
    $actorSymbol(current) = $get(n);
} else {
    $actorSymbol(current) = $actorSymbol(_generator).nextInt($get(n), $get(p));
}
$put(output, $actorSymbol(current))
/**/
