
/*** preinitBinomialSelectorBlock ***/
long $actorSymbol(sourcePool) = 0;
int * $actorSymbol(_current);
int $actorSymbol(trialsRemaining) = 0;
int $actorSymbol(selected) = 0;
double $actorSymbol(p) = 0;
/**/

/*** preinitBinomialSelectorArraysBlock($population) ***/
long $actorSymbol(sourceValues_$population) = 0;
/**/

/*** initBinomialSelectorBlock ***/
$actorSymbol(sourcePool) = 0;
$actorSymbol(selected) = 0;
$actorSymbol(sourcePool) = 0;
$actorSymbol(p) = 0;
/**/

/*** initArraysBinomialSelectorBlock($population) ***/
$actorSymbol(sourceValues_$population) = $ref(populations#$population);
if ($actorSymbol(sourceValues_$population) >= 0) {
    $actorSymbol(sourcePool) += $actorSymbol(sourceValues_$population);
}
/**/

/*** updateStateVariables($populationWidth) ***/
$actorSymbol(trialsRemaining) = $ref(trials);
$actorSymbol(_current) = calloc($populationWidth, sizeof(int));
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
        $actorSymbol(selected) = ColtRandomSource_BinomialDistribution(min($actorSymbol(trialsRemaining), (int)($actorSymbol(sourceValues_$num))), $actorSymbol(p), &$actorSymbol(current));
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
$ref(output#$channel) = $actorSymbol(_current)[$channel];
/**/
