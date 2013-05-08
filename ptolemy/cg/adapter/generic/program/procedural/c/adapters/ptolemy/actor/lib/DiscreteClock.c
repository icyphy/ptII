/***preinitBlock***/
static int $actorSymbol(phase);
static Time $actorSymbol(period);
static Time $actorSymbol(cycleStartTime);
static Time $actorSymbol(stopTime);
static $targetType(output) * $actorSymbol(values);
static Time * $actorSymbol(offsets);
static int $actorSymbol(offsetsNumber);
static boolean $actorSymbol(enabled);
static boolean $actorSymbol(triggered);
static Time $actorSymbol(nextOutputTime);
static int $actorSymbol(nextOutputIndex);
static boolean $actorSymbol(outputProduced);
/**/

/***initBlock($stopTime, $period, $offsetSize, $offsetList, $valuesSize, $valuesList)***/
int i = 0;

$actorSymbol(offsetsNumber) = $offsetSize;
$actorSymbol(offsets) = calloc($offsetSize, sizeof(Time));
$offsetList

$actorSymbol(values) = calloc($valuesSize, sizeof($type(output)));
$valuesList

$actorSymbol(period) = $period;
$actorSymbol(phase) = 0;
$actorSymbol(enabled) = true;
$actorSymbol(triggered) = true;
$actorSymbol(outputProduced) = false;
$actorSymbol(stopTime) = $stopTime;
$actorSymbol(cycleStartTime) = director.currentModelTime;
$actorSymbol(nextOutputTime) = $actorSymbol(cycleStartTime) + $actorSymbol(offsets)[$actorSymbol(phase)];
$actorSymbol(nextOutputIndex) = 1;

$fireAt(&director, $actorName(), $actorSymbol(nextOutputTime), $actorSymbol(nextOutputIndex));
/**/

/***startConnectedInit***/
// If the start port is connected, then start disabled.
$actorSymbol(enabled) = false;
/**/


/***startConnected***/
if ($hasToken(start#0)) {
	$get(start#0);
	// Restart everything.
	$actorName()InitializeCode();
	$actorSymbol(enabled) = true;
}
/**/

/***stopConnected***/
if ($hasToken(stop#0)) {
	$get(stop#0);
	// Stop the actor
	$actorSymbol(enabled) = false;
}
/**/

/***periodConnected***/
// Update the period from the port parameter, if appropriate.
if ($hasToken(period#0)) {
	$actorSymbol(period) = $get(period#0);
}
/**/

/***triggerConnected($channel)***/
if ($hasToken(trigger#$channel)) {
	$get(trigger#$channel);
	$actorSymbol(triggered) = true;
}
/**/

/***fireTestBlock***/

if (!$actorSymbol(enabled)) {
	// if the clock is disabled
	return;
}

double comparison = $actorSymbol(nextOutputTime) - director.currentModelTime;
if (comparison > 0) {
	return;
} else if (comparison == 0) {
	// It is the right time to produce an output. Check
	// the index.
	if ($actorSymbol(nextOutputIndex) > director.currentMicrostep) {
		// We have not yet reached the requisite index.
		// Request another firing at the current time.
		$fireAt(&director, $actorName(), director.currentModelTime, director.currentMicrostep + 1);
		return;
	}
	// At this point, the time matches the next output, and
	// the index either matches or exceeds the index for the next output,
	// or the director does not support superdense time.
	if (!$actorSymbol(triggered)) {
		// Pretend we produced an output so that postfire() will
		// skip to the next phase.
		$actorSymbol(outputProduced) = true;
		return;
	}
	// Ready to fire.
	if ($actorSymbol(enabled)) {
		$put(output, $actorSymbol(values)[$actorSymbol(phase)]);
		$actorSymbol(outputProduced) = true;
	}
	return;
}
/**/

/***postfireBlock($offsetSize, $triggerConnected)***/
if (director.currentModelTime > $actorSymbol(stopTime)) {
	return;
}
if ($actorSymbol(outputProduced)) {

	$actorSymbol(phase)++;
	if ($actorSymbol(phase) >= $actorSymbol(offsetsNumber)) {
		$actorSymbol(phase) = 0;
		$actorSymbol(cycleStartTime) += $actorSymbol(period);
	}
	if ($actorSymbol(offsets)[$actorSymbol(phase)] > $actorSymbol(period)) {
		perror("An offset of the Discrete Clock is greater than the period !");
		exit(1);
	}
	Time nextOutputTime = $actorSymbol(cycleStartTime) + $actorSymbol(offsets)[$actorSymbol(phase)];
	if ($actorSymbol(nextOutputTime) == nextOutputTime) {
		$actorSymbol(nextOutputIndex)++;
	} else {
		$actorSymbol(nextOutputTime) = nextOutputTime;
		$actorSymbol(nextOutputIndex) = 1;
	}
	$fireAt(&director, $actorName(), $actorSymbol(nextOutputTime), $actorSymbol(nextOutputIndex));

	$actorSymbol(outputProduced) = false;
	if ($triggerConnected) {
		$actorSymbol(triggered) = false;
	}
}
return;
/**/

/***wrapupBlock***/
free($actorSymbol(values));
free($actorSymbol(offsets));
/**/
