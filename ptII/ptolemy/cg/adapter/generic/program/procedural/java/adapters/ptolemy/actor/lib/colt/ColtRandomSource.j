/***preinitBlock***/
RandomElement $actorSymbol(_randomNumberGenerator);
int $actorSymbol(current);
double $actorSymbol(seed);
/**/

/*** setSeedBlock0($hashCode) ***/
$actorSymbol(seed) = System.currentTimeMillis() + $hashCode;
$actorSymbol(current) = (int)$actorSymbol(seed);
/**/

/*** setSeedBlock1($hashCode)***/
$actorSymbol(seed) = $val(seed) + $hashCode;
$actorSymbol(current) = (int)$actorSymbol(seed);
/**/


/*** setRandomNumberGeneratorDRand ***/
        $actorSymbol(_randomNumberGenerator) = new DRand((int) $actorSymbol(seed));
/**/

/*** setRandomNumberGeneratorMersenneTwister ***/
        $actorSymbol(_randomNumberGenerator) = new MersenneTwister((int) $actorSymbol(seed));
/**/

/*** setRandomNumberGeneratorRanecu ***/
        $actorSymbol(_randomNumberGenerator) = new Ranecu((int) $actorSymbol(seed));
/**/

/*** setRandomNumberGeneratorRanlux ***/
        $actorSymbol(_randomNumberGenerator) = new Ranlux((int) $actorSymbol(seed));
/**/

/*** setRandomNumberGeneratorRanmar ***/
        $actorSymbol(_randomNumberGenerator) = new Ranmar((int) $actorSymbol(seed));
/**/


/*** gaussianBlock ***/
/**/

/*** poissonBlock ***/
/**/


