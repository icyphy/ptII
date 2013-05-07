/***preinitBlock***/
int $actorSymbol(phase);
Time $actorSymbol(period);
double * $actorSymbol(values);
Time $actorSymbol(meanTime);
static long $actorSymbol(seed);
// this is the next function of java random
#define nextRandom(i) ((int) ((unsigned int) (($actorSymbol(seed) = (($actorSymbol(seed) * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1))) >> (48 - (i)))))
#define nextDouble() ((((long) nextRandom(26) << 27) + nextRandom(27)) / (double) (1L << 53))
$include(<time.h>)
/**/

/***initBlock($stopTime, $meanTime, $fireAtStart, $valuesSize, $valuesList, $seed)***/
$actorSymbol(meanTime) = $meanTime;

$actorSymbol(values) = calloc($valuesSize, sizeof(double));
$valuesList

Time currentTime = 0.0;
double randomNumber = 0.0;
$actorSymbol(seed) = 0;
// We have to recode the random function to have the same behavior as java Random
if ($seed == 0)
	$actorSymbol(seed) = time(NULL); // initialisation of rand
else
	$actorSymbol(seed) = ($seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);

if ($fireAtStart) {
	$fireAt(&director, $actorName(), currentTime, 0);
}
else {
	randomNumber = -log(1.0 - nextDouble()) * $actorSymbol(meanTime);
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
// randomNumber is now exponential with a meanTime specified
randomNumber = -log(1.0 - nextDouble()) * $actorSymbol(meanTime);
$fireAt(&director, $actorName(), currentTime + randomNumber, 0);
/**/

/***wrapupBlock***/
free($actorSymbol(values));
/**/
