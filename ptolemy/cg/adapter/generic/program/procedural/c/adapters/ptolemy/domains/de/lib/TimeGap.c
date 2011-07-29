/***fireBlock***/
static Time lastModelTime;
Time timeGap;
timeSub(currentModelTime, lastModelTime, &timeGap);
$put(output, timeGap);
lastModelTime = currentModelTime;
/**/
