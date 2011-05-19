/*** preinitBlock ***/
long $actorSymbol(seed);
/**/

/*** setSeedBlock0($hashCode) ***/
$actorSymbol(seed) = System.currentTimeMillis() + $hashCode;
$actorSymbol(_random) = new Random((long)$actorSymbol(seed));
/**/

/*** setSeedBlock1($hashCode) ***/
$actorSymbol(seed) = (((long) $val(seed) + $hashCode));
$actorSymbol(_random) = new Random((long)$actorSymbol(seed));
/**/




