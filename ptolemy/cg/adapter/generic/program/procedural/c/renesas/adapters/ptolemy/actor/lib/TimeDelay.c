/*** fireBlock($delayInitValue) ***/
int16 lastMicrostep = currentMicrostep;
static Time lastModelTime;
static double delayValue = $delayInitValue;
lastModelTime = currentModelTime;

if ($hasToken(delay)) {
        delayValue = $get(delay);
}

currentModelTime.secs += (int) delayValue;
currentModelTime.nsecs += delayValue - (int) delayValue;
if (currentModelTime.nsecs >= 1000000000) {
    currentModelTime.nsecs -= 1000000000;
    currentModelTime.secs++;
}

currentMicrostep = 0;

if ($hasToken(input)) {
        $put(output#0, $get(input));
}

currentModelTime = lastModelTime;
currentMicrostep = lastMicrostep;

/**/
