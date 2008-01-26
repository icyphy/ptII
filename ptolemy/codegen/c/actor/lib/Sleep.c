/***preinitBlock***/
#ifdef __AVR__
    long $actorSymbol(sleepTime);
#else
    struct timespec $actorSymbol(sleepTime);
    struct timespec $actorSymbol(remainingTime);
#endif /* __AVR__ */
/**/

/***initBlock***/
#ifndef __AVR__
    $actorSymbol(sleepTime).tv_sec = $ref(sleepTime) / 1000;
    $actorSymbol(sleepTime).tv_nsec = ($ref(sleepTime) % 1000) * 1000000;
#endif /* __AVR__ */

/**/

/*** fireBlock ***/
#ifdef __AVR__
    delay(&$actorSymbol(sleepTime));
#else
    nanosleep(&$actorSymbol(sleepTime), &$actorSymbol(remainingTime));

#endif /* __AVR__ */
/**/

/***transferBlock($channel)***/
    $ref(output#$channel) = $ref(input#$channel);
/**/

