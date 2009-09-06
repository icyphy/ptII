/***fireBlock***/
// The fire method of this actor assumes a Time structure, and also a getRealTime(Time*) method,
// which puts the current time into the Time* struct that's passed in.
Time time;
getRealTime(&time);
$put(output, time);
/**/