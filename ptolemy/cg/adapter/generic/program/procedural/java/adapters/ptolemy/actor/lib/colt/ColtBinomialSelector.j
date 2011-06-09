/***preinitBlock***/
Binomial $actorSymbol(_generator);
$super();
/**/

/*** preinitBinomialSelectorBlock ***/
long $actorSymbol(sourcePool) = 0;
int [] $actorSymbol(_current);
int $actorSymbol(trialsRemaining) = 0;
int $actorSymbol(selected) = 0;
double $actorSymbol(p) = 0;
/**/

/*** preinitBinomialSelectorArraysBlock($population) ***/
long $actorSymbol(sourceValues_$population) = 0;
/**/

/*** initBinomialSelectorBlock ***/
$actorSymbol(_generator) = new Binomial(1, 0.5, $actorSymbol(_randomNumberGenerator));
$actorSymbol(sourcePool) = 0;
$actorSymbol(selected) = 0;
$actorSymbol(sourcePool) = 0;
$actorSymbol(p) = 0;
/**/

/*** initArraysBinomialSelectorBlock($population) ***/
$actorSymbol(sourceValues_$population) = $get(populations#$population);
if ($actorSymbol(sourceValues_$population) >= 0) {
    $actorSymbol(sourcePool) += $actorSymbol(sourceValues_$population);
}
/**/

/*** updateStateVariables($populationWidth) ***/
$actorSymbol(trialsRemaining) = $get(trials);
$actorSymbol(_current) = new int[$populationWidth];
if ($actorSymbol(trialsRemaining) > $actorSymbol(sourcePool)) {
   $actorSymbol(trialsRemaining) = (int)($actorSymbol(sourcePool));
}

while($actorSymbol(trialsRemaining) > 0) {
/**/

/*** binomialSelectorBlock($num) ***/
$actorSymbol(selected) = 0;
if (($actorSymbol(trialsRemaining) > 0) && ($actorSymbol(sourceValues_$num) > 0)) {
    $actorSymbol(p) = (double) $actorSymbol(sourceValues_$num) / (double) $actorSymbol(sourcePool);
    if ($actorSymbol(p) < 1.0) {
        $actorSymbol(selected) = $actorSymbol(_generator).nextInt(Math.min($actorSymbol(trialsRemaining), (int)($actorSymbol(sourceValues_$num))), $actorSymbol(p));
    } else {
        $actorSymbol(selected) = $actorSymbol(trialsRemaining);
    }
}
$actorSymbol(_current)[$num] += $actorSymbol(selected);
$actorSymbol(sourceValues_$num) -= $actorSymbol(selected);
$actorSymbol(trialsRemaining) -= $actorSymbol(selected);
$actorSymbol(sourcePool) -= $actorSymbol(selected);
/**/

/***fireBlock($channel)***/
/* ColtBinomialSelector fireBlock $channel */
$put(output#$channel, $actorSymbol(_current)[$channel]);
/**/
