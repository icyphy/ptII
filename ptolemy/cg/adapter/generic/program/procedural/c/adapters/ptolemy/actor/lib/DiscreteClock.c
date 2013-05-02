/***preinitBlock***/
int $actorSymbol(phase);
Time $actorSymbol(period);
double * $actorSymbol(values);
// FIXME : change the double here to the relevant type
Time * $actorSymbol(offsets);
/**/

/***initBlock($stopTime, $period, $offsetSize, $offsetList, $valuesSize, $valuesList)***/
int i = 0;
$actorSymbol(offsets) = calloc($offsetSize, sizeof(Time));
$offsetList

$actorSymbol(values) = calloc($valuesSize, sizeof(double));
$valuesList

Time currentTime = 0.0;
while (currentTime <= $stopTime) {
	for (i = 0 ; i < $offsetSize ; i++) {
		$fireAt(&director, $actorName(), currentTime + $actorSymbol(offsets)[i], 0);
	}
	currentTime += $period;
}
$actorSymbol(phase) = 0;

/**/

/***fireBlock***/
$put(output, $actorSymbol(values)[$actorSymbol(phase)]);
/**/

/***postfireBlock($offsetSize)***/
$actorSymbol(phase)++;
$actorSymbol(phase) %= $offsetSize;
/**/

/***wrapupBlock***/
free($actorSymbol(values));
free($actorSymbol(offsets));
/**/
