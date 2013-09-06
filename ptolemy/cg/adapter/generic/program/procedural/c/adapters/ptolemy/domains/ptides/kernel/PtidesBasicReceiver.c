/*** createEvent($type, $convertedValue, $sinkFireName, $sinkPortName,
 $depth, $relativeDeadlineSecs, $relativeDeadlineNsecs,
 $offsetTimeSecs, $offsetTimeNsecs) ***/
{
Time dummyTime;
Event* event = newEvent();
event->tag.timestamp = currentModelTime;
event->tag.microstep = currentMicrostep;
event->depth = $depth;
dummyTime.secs = $relativeDeadlineSecs;
dummyTime.nsecs = $relativeDeadlineNsecs;
timeAdd(event->tag.timestamp, dummyTime, &(event->deadline));
event->offsetTime.secs = $offsetTimeSecs;
event->offsetTime.nsecs = $offsetTimeNsecs;
event->fireMethod = $sinkFireName;
event->Val.$type_Value = $convertedValue
event->sinkEvent = &($sinkPortName);
addEvent(event);
}
/**/
