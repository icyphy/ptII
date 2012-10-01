/***fireBlock***/
int intPart;
double fractPart;
int lastMicrostep = currentMicrostep;
static Time lastModelTime;
static double delayValue;

lastModelTime = currentModelTime;

if ($hasToken(delay)) {
        delayValue = $get(delay);
}
currentModelTime.secs += (int) delayValue; // intPart
fractPart = delayValue - (int) delayValue;
currentModelTime.nsecs += (int) (fractPart * 1000000000.0);
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
