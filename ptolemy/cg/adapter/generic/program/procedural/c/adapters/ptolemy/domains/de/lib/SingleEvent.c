/***initBlock($intPart, $fracPart, $value)***/
int lastMicrostep = currentMicrostep;
static Time lastModelTime;
lastModelTime = currentModelTime;

currentModelTime.secs = $intPart;
currentModelTime.nsecs = $fracPart;
if (currentModelTime.nsecs >= 1000000000) {
    currentModelTime.nsecs -= 1000000000;
    currentModelTime.secs++;
}

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$put(output#0, $value);

currentModelTime = lastModelTime;
currentMicrostep = lastMicrostep;
/**/

/***fireBlock***/
/**/