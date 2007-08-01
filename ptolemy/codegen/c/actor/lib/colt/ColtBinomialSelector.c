
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
$ref(output#$num) = $actorSymbol(selected);
$actorSymbol(trialsRemaining) -= $actorSymbol(selected);
$actorSymbol(sourcePool) -= $actorSymbol(sourceValues_$num);
/**/

/*** reference ***/
int trials
long populations[]

nextint()
long sourceValues[populations.length]
long sourceTotal = 0
for (i from 0 to sourceValues.length) {
	sourceValues[i] = populations[i]
	if (sourceValues[i] < 0) {
		break;
	}
	sourceTotal += sourceValues[i]
}

int trialsRemaining = trials
long sourcePool = sourceTotal
_current.length = sourceValues.length
int selected
double p
for (i from 0 to _current.length) {
	selected = 0
	if ((trialsRemaining > 0) && (sourceValues[i] > 0)) {
		p = sourceValues[i] / sourcePool
		if (p < 1.0) {
			selected = binomial(trialsRemaining, p)
		} else {
			selected = trialsRemaining
		}
	}
	current[i] = selected
	trialsRemaining -= selected
	sourcePool -= sourceValues[i]
}
/**/

