/***fireBlock***/
int lastMicrostep = currentMicrostep;
Time lastModelTime;
double intDelay;
double fracDelay;

timeSet(currentModelTime, &lastModelTime);

fracDelay = modf($val(delay), &intDelay);
currentModelTime.secs += (int)intDelay;
//FIXME: update this multiplication to shifts
currentModelTime.nsecs += (int)(fracDelay * 1000000000);

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$send(output#0, $get(input));

timeSet(lastModelTime, &currentModelTime);
currentMicrostep = lastMicrostep;
/**/
