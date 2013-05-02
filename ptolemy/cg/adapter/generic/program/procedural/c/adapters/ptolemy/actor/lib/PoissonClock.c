/***preinitBlock***/
int $actorSymbol(phase);
Time $actorSymbol(period);
double * $actorSymbol(values);
Time $actorSymbol(meanTime);
$include(<time.h>)
// FIXME : change the double here to the relevant type
/**/

/***initBlock($stopTime, $meanTime, $fireAtStart, $valuesSize, $valuesList)***/
$actorSymbol(meanTime) = $meanTime;

$actorSymbol(values) = calloc($valuesSize, sizeof(double));
$valuesList

Time currentTime = 0.0;
double randomNumber = 0.0;
srand(time(NULL)); // initialisation of rand
// randomNumber is uniform between 0.0 and 1.0
randomNumber = (rand()/(double)RAND_MAX);
// randomNumber is now exponential with a meanTime specified
randomNumber = -log(1.0 - randomNumber) * $actorSymbol(meanTime);

if ($fireAtStart) {
	$fireAt(&director, $actorName(), currentTime, 0);
}
else {
	$fireAt(&director, $actorName(), currentTime + randomNumber, 0);
}

$actorSymbol(phase) = 0;

/**/

/***fireBlock***/
$put(output, $actorSymbol(values)[$actorSymbol(phase)]);
/**/

/***postfireBlock($offsetSize)***/
$actorSymbol(phase)++;
$actorSymbol(phase) %= $offsetSize;

Time currentTime = director.currentModelTime;
double randomNumber = 0.0;
// randomNumber is uniform between 0.0 and 1.0
randomNumber = (rand()/(double)RAND_MAX);
// randomNumber is now exponential with a meanTime specified
randomNumber = -log(1.0 - randomNumber) * $actorSymbol(meanTime);
$fireAt(&director, $actorName(), currentTime + randomNumber, 0);
/**/

/***wrapupBlock***/
free($actorSymbol(values));
/**/
