
/*** preinitBinomialSelectorBlock ***/
long $actorSymbol(sourceTotal) = 0;

int $actorSymbol(trialsRemaining) = 0;
int $actorSymbol(selected) = 0;
long $actorSymbol(sourcePool) = 0;
double $actorSymbol(p) = 0;
/**/

/*** preinitBinomialSelectorArraysBlock($population) ***/
long $actorSymbol(sourceValues_$population) = 0;
/**/

/*** initBinomialSelectorBlock ***/
$actorSymbol(sourceTotal) = 0;
$actorSymbol(trialsRemaining) = $ref(trials);
$actorSymbol(selected) = 0;
$actorSymbol(sourcePool) = 0;
$actorSymbol(p) = 0;
/**/

/*** initArraysBinomialSelectorBlock($population) ***/
$actorSymbol(sourceValues_$population) = $ref(populations#$population);
if ($actorSymbol(sourceValues_$population) >= 0) {
    $actorSymbol(sourceTotal) = $actorSymbol(sourceTotal) + $ref(populations#$population);
    $actorSymbol(sourcePool) = $actorSymbol(sourcePool) + $ref(populations#$population);
}
/**/

/*** binomialSelectorBlock($num) ***/
if (($actorSymbol(trialsRemaining) > 0) && ($actorSymbol(sourceValues_$num) > 0)) {
    $actorSymbol(p) = (double) $actorSymbol(sourceValues_$num) / (double) $actorSymbol(sourcePool);
    if ($actorSymbol(p) < 1.0) {
        $actorSymbol(selected) = ColtRandomSource_BinomialDistribution($actorSymbol(trialsRemaining), $actorSymbol(p), &$actorSymbol(current));
    } else {
        $actorSymbol(selected) = $actorSymbol(trialsRemaining);
    }
}

$actorSymbol(trialsRemaining) -= $actorSymbol(selected);
$actorSymbol(sourcePool) -= $actorSymbol(sourceValues_$num);
/**/

/***fireBlock($num)***/
$ref(output#$num) = $actorSymbol(selected);
/**/
