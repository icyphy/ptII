/***preinitBlock***/
Time $actorSymbol(previousTime);
/**/

/***initBlock***/
$actorSymbol(previousTime) = -DBL_MAX;
/**/

/***fireBlock***/
// Consume an input.
if ($hasToken(input)) {
        $get(input);
}
struct Director* director = (*(actor->getDirector))(actor);
Time currentTime = (*(director->getModelTime))(director);

if ($actorSymbol(previousTime) != -DBL_MAX) {
        Time outToken = currentTime - $actorSymbol(previousTime);
        $put(output, outToken);
}
/**/

/***postfireBlock***/
struct Director* director = (*(actor->getDirector))(actor);
$actorSymbol(previousTime) = (*(director->getModelTime))(director);
/**/
