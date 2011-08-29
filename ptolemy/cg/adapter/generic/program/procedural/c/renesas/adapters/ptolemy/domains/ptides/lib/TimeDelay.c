/***fireBlock($intPart, $fracPart)***/
int16 lastMicrostep = currentMicrostep;
static Time lastModelTime;
lastModelTime = currentModelTime;

currentModelTime.secs += $intPart;
currentModelTime.nsecs += $fracPart;

if (currentModelTime.nsecs >= 1000000000) {
    currentModelTime.nsecs -= 1000000000;
    currentModelTime.secs++;
}

currentMicrostep = 0;

$put(output#0, $get(input));

currentModelTime = lastModelTime;
currentMicrostep = lastMicrostep;

/**/
