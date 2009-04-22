/*** createEvent($type, $convertedValue, $timestamp, $microstep) ***/
Event* event = newEvent();
timeSet(event->tag.timestamp, currentTime);
event->tag.microstep = currentMicrostep;
event->$type_Value = $convertedValue;
eventAdd(event);
/**/
