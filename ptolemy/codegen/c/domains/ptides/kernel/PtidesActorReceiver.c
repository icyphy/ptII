/*** createEvent($type, $convertedValue, $sinkFireName, $sinkPortName,
 $timestamp, $microstep, $relativeDeadline, $offsetTime) ***/
{
Event* event = newEvent();
timeSet(currentModelTime, &(event->tag.timestamp));
event->tag.microstep = currentMicrostep;
timeAdd(event->tag.timestamp, $relativeDeadline, &(event->deadline));
event->offsetTime = $offsetTime;
event->fireMethod = $sinkFireName;
event->Val.$type_Value = $convertedValue;
event->sinkEvent = $sinkPortName;
addEvent(event);
}
/**/
