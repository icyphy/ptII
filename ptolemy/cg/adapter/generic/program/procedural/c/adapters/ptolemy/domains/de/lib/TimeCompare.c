/***preinitBlock***/
$structure(DoubleQueue)

/** A boolean parameter to decide whether inputs at input2 should be ignored
if they lead to negative outputs. */
boolean $actorSymbol(nonnegative);

/** The list to store the time stamps received at input1 but have never been
compared. */
static DoubleQueue $actorSymbol(input1TimeStamps);

/** The list to store the time stamps received at input2 but have never been
compared. */
static DoubleQueue $actorSymbol(input2TimeStamps);
/**/

/***initializeBlock***/
$actorSymbol(nonnegative) = $val(nonnegative);
DoubleQueueClear(&$actorSymbol(input1TimeStamps));
DoubleQueueClear(&$actorSymbol(input2TimeStamps));
/**/

/*** prefireBlock ***/
return ($hasToken(input1) || $hasToken(input2));
/**/

/***fireBlock***/
Time currentTime = director.currentModelTime;

while ($hasToken(input1)) {
	$get(input1);
	DoubleQueuePut(&$actorSymbol(input1TimeStamps), currentTime);
}

while ($hasToken(input2)) {
	$get(input2);
	DoubleQueuePut(&$actorSymbol(input2TimeStamps), currentTime);
}

while (DoubleQueueHasToken(&$actorSymbol(input1TimeStamps)) && DoubleQueueHasToken(&$actorSymbol(input2TimeStamps))) {
	double input1 = DoubleQueueGet(&$actorSymbol(input1TimeStamps));
	double input2 = DoubleQueueTake(&$actorSymbol(input2TimeStamps));

	double difference = input2 - input1;

	if ($actorSymbol(nonnegative)) {
		while (difference < 0.0 && DoubleQueueHasToken(&$actorSymbol(input2TimeStamps))) {
			input2 = DoubleQueueTake(&$actorSymbol(input2TimeStamps));
			difference = input2 - input1;
		}
		if (difference >= 0.0) {
			DoubleQueueTake(&$actorSymbol(input1TimeStamps));
			$put(output, difference);
		}
	} else {
		DoubleQueueTake(&$actorSymbol(input1TimeStamps));
		$put(output, difference);
	}
}
/**/

/*** wrapupBlock ***/
DoubleQueueClear(&$actorSymbol(input1TimeStamps));
DoubleQueueClear(&$actorSymbol(input2TimeStamps));
/**/
