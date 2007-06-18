/***preinitBlock***/
    struct timespec $actorSymbol(sleepTime);
    struct timespec $actorSymbol(remainingTime);
/**/

/***initBlock***/
    $actorSymbol(sleepTime).tv_sec = $val(sleepTime) / 1000;
    $actorSymbol(sleepTime).tv_nsec = ($val(sleepTime) % 1000) * 1000000;
/**/

/*** fireBlock ***/
    nanosleep(&$actorSymbol(sleepTime), &$actorSymbol(remainingTime));
/**/

/***transferBlock($channel)***/
    $ref(output#$channel) = $ref(input#$channel);
/**/

