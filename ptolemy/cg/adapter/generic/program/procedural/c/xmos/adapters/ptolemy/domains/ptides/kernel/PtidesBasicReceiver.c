/*** createEvent($sourceTimeString, $type, $convertedValue, $sinkFireName, $sinkPortName,
 $depth, $relativeDeadlineSecs, $relativeDeadlineNsecs,
 $offsetTimeSecs, $offsetTimeNsecs) ***/
    {
            Time dummyTime, timestamp;
            Event event;

            Time* sourceTime;

            $sourceTimeString;
        event.tag.timestamp = *sourceTime;
        //fixme
        event.tag.microstep = 0;

        timestampAdd(&event.tag.timestamp, &(addTime), &timestamp);
        event.tag.timestamp = timestamp;

        event.depth = $depth;
        dummyTime.secs = $relativeDeadlineSecs;
        dummyTime.nsecs = $relativeDeadlineNsecs;
        timestampAdd(&event.tag.timestamp, &dummyTime, &event.deadline);
        event.offset.secs = $offsetTimeSecs;
        event.offset.nsecs = $offsetTimeNsecs;
        event.fire = $sinkFireName;
        event.value.$type_Value = $convertedValue;
        event.sinkEvent = &($sinkPortName);
        asm("clrsr 0x2"); addEvent(schedulerChanend, &event); asm("setsr 0x2");
    };

/**/
