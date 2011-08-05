/***fireBlock***/
static Time lastModelTime;
Time timeGap;
timeSub(currentModelTime, lastModelTime, &timeGap);
$put(output, timeGap.secs + timeGap.nsecs/1000000000);
lastModelTime = currentModelTime;
/**/
