/***initBlock($intPart, $fracPart, $value)***/
currentModelTime.secs = $intPart;
currentModelTime.nsecs = $fracPart;

//FIXME: this is not necessarily correct
currentMicrostep = 0;
$put(output#0, $value);
/**/

/***fireBlock***/
/**/