/***preinitBlock***/
int $actorSymbol(phase);
Time $actorSymbol(period);
Time $actorSymbol(cycleStartTime);
Time $actorSymbol(stopTime);
$targetType(output) * $actorSymbol(values);
Time * $actorSymbol(offsets);
int $actorSymbol(offsetsNumber);
boolean $actorSymbol(enabled);
boolean $actorSymbol(triggered);
Time $actorSymbol(nextOutputTime);
int $actorSymbol(nextOutputIndex);
boolean $actorSymbol(outputProduced);
/**/

/***initBlock($stopTime, $period, $offsetSize, $offsetList, $valuesSize, $valuesList)***/
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
$actorSymbol(cycleStartTime) = $DirectorName()->currentModelTime;
$actorSymbol(nextOutputTime) = $actorSymbol(cycleStartTime) + $actorSymbol(offsets)[$actorSymbol(phase)];
$actorSymbol(nextOutputIndex) = 1;

$fireAt($ModelName()_$actorName(), $actorSymbol(nextOutputTime), $actorSymbol(nextOutputIndex));
/**/

/***startConnectedInit***/
// If the start port is connected, then start disabled.
$actorSymbol(enabled) = false;
/**/


/***startConnected***/
if ($hasToken(start#0)) {
	(void)$get(start#0);
	// Restart everything.
	$ModelName()_$actorName()_initialize();
	$actorSymbol(enabled) = true;
}
/**/

/***stopConnected***/
if ($hasToken(stop#0)) {
	(void)$get(stop#0);
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
	(void)$get(trigger#$channel);
	$actorSymbol(triggered) = true;
}
/**/

/***fireTestBlock***/

if (!$actorSymbol(enabled)) {
	// if the clock is disabled
	return;
}

double comparison = $actorSymbol(nextOutputTime) - $DirectorName()->currentModelTime;
if (comparison > 0) {
	return;
} else if (comparison == 0) {
	// It is the right time to produce an output. Check
	// the index.
	if ($actorSymbol(nextOutputIndex) > $DirectorName()->currentMicrostep) {
		// We have not yet reached the requisite index.
		// Request another firing at the current time.
		$fireAt($ModelName()_$actorName(), $DirectorName()->currentModelTime, $DirectorName()->currentMicrostep + 1);
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
if ($DirectorName()->currentModelTime > $actorSymbol(stopTime)) {
	return true;
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
	$fireAt($ModelName()_$actorName(), $actorSymbol(nextOutputTime), $actorSymbol(nextOutputIndex));

	$actorSymbol(outputProduced) = false;
	if ($triggerConnected) {
		$actorSymbol(triggered) = false;
	}
}
return true;
/**/

/***wrapupBlock***/
free($actorSymbol(values));
free($actorSymbol(offsets));
/**/
