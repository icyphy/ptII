/*** createEvent($type, $convertedValue, $sinkFireName, $sinkPortName,
 $timestamp, $microstep, $relativeDeadlineSecs, $relativeDeadlineNsecs,
 $offsetTimeSecs, $offsetTimeNsecs) ***/
{
Time dummyTime;
Event* event = newEvent();
timeSet(currentModelTime, &(event->tag.timestamp));
event->tag.microstep = currentMicrostep;
dummyTime.secs = $relativeDeadlineSecs;
dummyTime.nsecs = $relativeDeadlineNsecs;
timeAdd(event->tag.timestamp, dummyTime, &(event->deadline));
event->offsetTime.secs = $offsetTimeSecs;
event->offsetTime.nsecs = $offsetTimeNsecs;
event->fireMethod = $sinkFireName;
event->Val.$type_Value = $convertedValue;
event->sinkEvent = &($sinkPortName);
addEvent(event);
}
/**/
