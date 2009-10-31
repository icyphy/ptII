/***fireBlock***/
static uint8 $actorSymbol(hasFired) = 0;
static Time $actorSymbol(previousTime);

if($actorSymbol(hasFired)){
    Time $actorSymbol(currentTime);
    uint64 $actorSymbol(gap_ns);
    
    getRealTime(&$actorSymbol(currentTime));
    timeSub($actorSymbol(currentTime), $actorSymbol(previousTime), &$actorSymbol(gap));
    
    //Convert time to ns to invert without
    // floating point division
    $actorSymbol(gap_ns) = $actorSymbol(currentTime).nsecs + 1000000000*$actorSymbol(currentTime).secs;
    $put(output, 1000000000 / $actorSymbol(gap_ns));
}

$actorSymbol(hasFired) = 1;
timeSet($actorSymbol(currentTime), $actorSymbol(previousTime));
/**/
