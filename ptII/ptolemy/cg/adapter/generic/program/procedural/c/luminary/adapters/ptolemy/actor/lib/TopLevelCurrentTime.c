/***fireBlock***/
// The fire method of this actor assumes a Time structure, and also a getRealTime(Time*) method,
// which puts the current time into the Time* struct that's passed in.
Time time;
double doubleTime;
getRealTime(&time);
//FIXME: should use shifts instead of multiply
doubleTime = (double)(time.secs * 1000000000) + (double) time.nsecs;
$put(output, doubleTime);
/**/
