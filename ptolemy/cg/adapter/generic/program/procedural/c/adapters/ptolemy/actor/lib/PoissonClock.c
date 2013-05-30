/***preinitBlock***/
static int $actorSymbol(phase);
static Time $actorSymbol(period);
static double * $actorSymbol(values);
static Time $actorSymbol(meanTime);
static long $actorSymbol(seed);
static Time $actorSymbol(nextFiringTime);
static boolean $actorSymbol(needNew);
static double $actorSymbol(randomNumber);
static boolean $actorSymbol(outputProduced);
// this is the next function of java random
#define nextRandom(i) ((int) ((unsigned int) (($actorSymbol(seed) = (($actorSymbol(seed) * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1))) >> (48 - (i)))))
#define nextDouble() ((((long) nextRandom(26) << 27) + nextRandom(27)) / (double) (1L << 53))
$include(<time.h>)
/**/

/***initBlock($stopTime, $meanTime, $fireAtStart, $valuesSize, $valuesList, $privateSeed, $seed)***/
$actorSymbol(meanTime) = $meanTime;

$actorSymbol(values) = calloc($valuesSize, sizeof(double));
$valuesList

Time currentTime = 0.0;
double randomNumber = 0.0;
$actorSymbol(seed) = 0;
$actorSymbol(needNew) = false;
$actorSymbol(randomNumber) = 0.0;
$actorSymbol(outputProduced) = false;
// We have to recode the random function to have the same behavior as java Random
if ($privateSeed == 0 && $seed == 0)
	$actorSymbol(seed) = time(NULL); // initialisation of rand
else
	if ($privateSeed == 0)
		$actorSymbol(seed) = ($seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
	else
		$actorSymbol(seed) = ($privateSeed ^ 0x5DEECE66DL) & ((1L << 48) - 1);

if ($fireAtStart) {
	$fireAt($ModelName()_$actorName(), currentTime, 0);
}
else {
	$actorSymbol(randomNumber) = -log(1.0 - nextDouble()) * $actorSymbol(meanTime);
	$fireAt($ModelName()_$actorName(), currentTime + $actorSymbol(randomNumber), 0);
}

$actorSymbol(phase) = 0;

/**/

/***fireBlockTrigger($channel)***/
if ($hasToken(trigger#$channel)) {
	$get(trigger#$channel);
	triggerInputPresent = true;
}
/**/

/***fireBlockInit***/
// If there is a trigger input, then it is time for an output.
boolean triggerInputPresent = false;
/**/


/***fireBlockEnd***/

// It is time to produce an output if the current time equals
// or exceeds the next firing time (it should never exceed).
boolean timeForOutput = $DirectorName()->currentModelTime - $actorSymbol(nextFiringTime) >= 0;

if (!timeForOutput && !triggerInputPresent) {
	// It is too early.
	return;
}
if ($DirectorName()->currentMicrostep < 1 && !triggerInputPresent) {
	// The time matches, but the microstep is too early.
	return;
}

if ($actorSymbol(needNew)) {
	$actorSymbol(randomNumber) = -log(1.0 - nextDouble()) * $actorSymbol(meanTime);
	$actorSymbol(needNew) = false;
}

$put(output, $actorSymbol(values)[$actorSymbol(phase)]);
$actorSymbol(outputProduced) = true;
/**/

/***postfireBlock($offsetSize)***/
$actorSymbol(needNew) = true;
if ($actorSymbol(outputProduced)) {
	// An output was produced in this iteration.
	$actorSymbol(outputProduced) = false;
	$actorSymbol(phase)++;
	$actorSymbol(phase) %= $offsetSize;

	$actorSymbol(nextFiringTime) = $DirectorName()->currentModelTime + $actorSymbol(randomNumber);
	$fireAt($ModelName()_$actorName(), $actorSymbol(nextFiringTime), 0);
} else if ($DirectorName()->currentModelTime - $actorSymbol(nextFiringTime) >= 0) {
	// Output was not produced, but time matches, which
	// means the microstep was too early. Request a refiring.
	$fireAt($ModelName()_$actorName(), $DirectorName()->currentModelTime, 0);
}

return;
/**/

/***wrapupBlock***/
free($actorSymbol(values));
/**/
