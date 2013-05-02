/***preinitBlock***/
$include(<float.h>)
// The time when the previous input arrives.
static Time $actorSymbol(previousTime);
/**/

/***initializeBlock***/
$actorSymbol(previousTime) = -DBL_MAX;
/**/

/***fireBlock***/
// Consume an input.
if ($hasToken(input)) {
	$get(input);
}

Time currentTime = director.currentModelTime;

if ($actorSymbol(previousTime) != -DBL_MAX) {
	Time outToken = currentTime - $actorSymbol(previousTime);
	$put(output, outToken);
}
/**/

/***postfireBlock***/
$actorSymbol(previousTime) = director.currentModelTime;
/**/
