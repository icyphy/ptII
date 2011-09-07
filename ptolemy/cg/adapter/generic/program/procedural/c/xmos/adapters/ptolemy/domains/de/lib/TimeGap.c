/***fireBlock($name)***/
    static Timestamp lastTimestamp;
    Timestamp* timestamp = &$name_input[0]->tag.timestamp;
    Timestamp timestampGap;
    double timeGap;

    timestampSub(timestamp, &lastTimestamp, &timestampGap);

    //debug("TimeGap = %u %u @ %u %u\n", timestampGap.s, timestampGap.ns, timestamp->s, timestamp->ns);
    timeGap = 0;
    // convert to double
    timeGap = timestampGap.ns;
    timeGap /= 1000000000;
    timeGap += timestampGap.s;

    $put(output#0, timeGap);


    lastTimestamp = *timestamp;
/**/
