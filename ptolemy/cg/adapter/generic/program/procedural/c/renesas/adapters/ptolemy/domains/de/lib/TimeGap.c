/***fireBlock***/
static Time lastModelTime;
Time timeGap;
subTime(currentModelTime, lastModelTime, &timeGap);
$put(output, timeGap.secs + timeGap.nsecs/1000000000.0);
lastModelTime = currentModelTime;
/**/
