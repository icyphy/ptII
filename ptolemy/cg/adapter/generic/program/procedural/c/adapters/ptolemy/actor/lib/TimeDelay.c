/***preinitBlock***/
PblList* $actorSymbol(pendingOutputs);
$targetType(delay) $actorSymbol(delay);
$include("_CalendarQueue.h")

struct PendingEvent {
        Time timestamp;
        Token token;
        int microstep;
};
/**/

/***initBlock($delayInit)***/
$actorSymbol(pendingOutputs) = pblListNewLinkedList();
$actorSymbol(delay) = $delayInit;
/**/

/***fireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
struct PendingEvent* currentCell = NULL;
if (pblListIsEmpty($actorSymbol(pendingOutputs)))
        return;

currentCell = pblListPeek($actorSymbol(pendingOutputs));
// if it is time to fire
if (currentCell->timestamp == (*(director->getModelTime))(director)
                && currentCell->microstep >= (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director)) {
        $put(output#0, currentCell->token.payload.$cgType(input));
        pblListPoll($actorSymbol(pendingOutputs));
}
/**/

/***postfireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
if ($hasToken(input)) {
        Time fireTime = (*(director->getModelTime))(director) + $actorSymbol(delay);
        int microstep = 1;
        if ($actorSymbol(delay) == 0)
                microstep = (*(((struct DEDirector*)director)->getMicrostep))((struct DEDirector*)director)+1;
        struct PendingEvent* newEvent = calloc(1, sizeof(struct PendingEvent));
        if (newEvent == NULL) {
                fprintf(stderr, "Allocation problem : TimeDelay_postfire");
                exit(1);
        }
        newEvent->microstep = microstep;
        newEvent->timestamp = fireTime;
        newEvent->token.payload.$cgType(input) = $get(input);
        newEvent->token.type = TYPE_$cgType(input);
        pblListPush($actorSymbol(pendingOutputs), newEvent);
        $fireAt(actor, fireTime, microstep);
}
/**/

/***wrapupBlock***/
pblListFree($actorSymbol(pendingOutputs));
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
