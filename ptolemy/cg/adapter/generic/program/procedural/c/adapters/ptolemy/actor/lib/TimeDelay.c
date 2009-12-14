/***fireBlock($intPart, $fracPart)***/
static int lastMicrostep = currentMicrostep;
static Time lastModelTime;

timeSet(currentModelTime, &lastModelTime);

currentModelTime.secs += $intPart;
currentModelTime.nsecs += $fracPart;
if (currentModelTime.nsecs >= 1000000000) {
    currentModelTime.nsecs -= 1000000000;
    currentModelTime.secs++;
}

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$put(output#0, $get(input));

timeSet(lastModelTime, &currentModelTime);
currentMicrostep = lastMicrostep;
/**/
