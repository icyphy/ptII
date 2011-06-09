/***fireBlock($intPart, $fracPart)***/
int lastMicrostep = currentMicrostep;
Time lastModelTime;

timeSet(currentModelTime, &lastModelTime);

currentModelTime.secs += $intPart;
currentModelTime.nsecs += $fracPart;

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$send(output#0, $get(input));

timeSet(lastModelTime, &currentModelTime);
currentMicrostep = lastMicrostep;
/**/
