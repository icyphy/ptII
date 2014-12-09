/***preinitBlock***/
int $actorSymbol(phase);
Time $actorSymbol(period);
double * $actorSymbol(values);
Time $actorSymbol(meanTime);
long $actorSymbol(seed);
Time $actorSymbol(nextFiringTime);
boolean $actorSymbol(needNew);
double $actorSymbol(randomNumber);
boolean $actorSymbol(outputProduced);
// This is the next function of java random.
// gcc give a warning here because the order of operations is not deterministic.
// The compiler is free to execute either side of the + opertion in nextDouble(), which
// will end up with different values. 
#define nextRandom(i) ((int) ((unsigned int) (($actorSymbol(seed) = (($actorSymbol(seed) * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1))) >> (48 - (i)))))
#define nextDouble() ((((long) nextRandom(26) << 27) + nextRandom(27)) / (double) (1L << 53))
$include(<time.h>)
/**/

/***initBlock($stopTime, $meanTime, $fireAtStart, $valuesSize, $valuesList, $privateSeed, $seed)***/
$actorSymbol(meanTime) = $meanTime;

$actorSymbol(values) = calloc($valuesSize, sizeof(double));
$valuesList

Time currentTime = 0.0;
$actorSymbol(seed) = 0;
$actorSymbol(needNew) = true;
$actorSymbol(randomNumber) = 0.0;
$actorSymbol(outputProduced) = false;

// We have to recode the random function to have the same behavior as java Random
if ($privateSeed == 0 && $seed == 0) {
        $actorSymbol(seed) = time(NULL); // initialisation of rand
} else {
    if ($privateSeed == 0) {
        $actorSymbol(seed) = ($seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    } else {
        $actorSymbol(seed) = ($privateSeed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    }
    if ($fireAtStart) {
        $fireAt(actor, currentTime, 0);
    } else {
        $actorSymbol(randomNumber) = -log(1.0 - nextDouble()) * $actorSymbol(meanTime);
        $fireAt(actor, currentTime + $actorSymbol(randomNumber), 0);
    }
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
struct Director* director = (*(actor->getDirector))(actor);
boolean timeForOutput = (*(director->getModelTime))(director) - $actorSymbol(nextFiringTime) >= 0;

if (!timeForOutput && !triggerInputPresent) {
        // It is too early.
        return;
}
if ((*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director) < 1 && !triggerInputPresent) {
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
struct Director* director = (*(actor->getDirector))(actor);
if ($actorSymbol(outputProduced)) {
        // An output was produced in this iteration.
        $actorSymbol(outputProduced) = false;
        $actorSymbol(phase)++;
        $actorSymbol(phase) %= $offsetSize;

        $actorSymbol(nextFiringTime) = (*(director->getModelTime))(director) + $actorSymbol(randomNumber);
        $fireAt(actor , $actorSymbol(nextFiringTime), 0);
} else if ((*(director->getModelTime))(director) - $actorSymbol(nextFiringTime) >= 0) {
        // Output was not produced, but time matches, which
        // means the microstep was too early. Request a refiring.
        $fireAt(actor, (*(director->getModelTime))(director), 0);
}

return true;
/**/

/***wrapupBlock***/
free($actorSymbol(values));
/**/
