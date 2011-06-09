/*** preinitBlock ***/
long $actorSymbol(seed);
/**/

/*** setSeedBlock0($hashCode) ***/
$actorSymbol(seed) = time (NUl) + $hashCode;
/**/

/*** setSeedBlock1($hashCode) ***/
$actorSymbol(seed) = (((long) $val(seed) + $hashCode));
$actorSymbol(_random) = new Random((long)$actorSymbol(seed));
/**/




