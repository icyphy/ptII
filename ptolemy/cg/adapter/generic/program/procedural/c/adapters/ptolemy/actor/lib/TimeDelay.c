/***preinitBlock***/
CQLinkedList* $actorSymbol(pendingOutputs);
$targetType(delay) $actorSymbol(delay);
/**/

/***initBlock($delayInit)***/
$actorSymbol(pendingOutputs) = newCQLinkedList();
$actorSymbol(delay) = $delayInit;
/**/

/***fireBlock***/
CQCell* currentCell = NULL;
if (CQLinkedListIsEmpty($actorSymbol(pendingOutputs)))
	return;

currentCell = malloc(sizeof(CQCell));
if (currentCell == NULL) {
	perror("Allocation problem (TimeDelayFire)");
	exit(1);
}
currentCell->content = CQLinkedListGet($actorSymbol(pendingOutputs));
// if it is time to fire
if (currentCell->content->timestamp == director.currentModelTime
		&& currentCell->content->microstep >= director.currentMicrostep) {
	$put(output#0, currentCell->content->token.payload.$cgType(input));
	CQLinkedListTake($actorSymbol(pendingOutputs));
}
/**/

/***postfireBlock***/
if ($hasToken(input)) {
	Time fireTime = director.currentModelTime + $actorSymbol(delay);
	int microstep = 1;
	if ($actorSymbol(delay) == 0)
		microstep = director.currentMicrostep+1;
	DEEvent * newEvent = newDEEventWithParam(director.currentActor, NULL,
			director.currentActor->depth,
			microstep, director.currentActor->priority, fireTime);
	newEvent->token.payload.$cgType(input) = $get(input);
	CQLinkedListInsert($actorSymbol(pendingOutputs), newEvent);
	$fireAt(&director, $actorName(), fireTime, microstep);
}
/**/

/***wrapupBlock***/
CQLinkedListDelete($actorSymbol(pendingOutputs));
/**/


/*int lastMicrostep = currentMicrostep;
static Time lastModelTime;

lastModelTime = currentModelTime;

currentModelTime.secs += $intPart;
currentModelTime.nsecs += $fracPart;
if (currentModelTime.nsecs >= 1000000000) {
    currentModelTime.nsecs -= 1000000000;
    currentModelTime.secs++;
}

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$put(output#0, $get(input));

currentModelTime = lastModelTime;
currentMicrostep = lastMicrostep;
*/
