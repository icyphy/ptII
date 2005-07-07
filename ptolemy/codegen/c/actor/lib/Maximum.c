/*** preinitBlock ***/
    int $actorSymbol(i);
/**/

/*** initMax ***/
    $ref(maximumValue) = $ref(input#0);
/**/

/*** initChannelNum ***/
    $ref(channelNumber) = -1;
/**/

/*** fireBlock(<arg>) ***/
    for ($actorSymbol(i) = 0; $actorSymbol(i) < <arg>; $actorSymbol(i)++) {
        if ($ref(input#$actorSymbol(i)) > $ref(maximumValue)) {
            $ref(maximumValue) =  $ref(input#$actorSymbol(i));
            $ref(channelNumber) = $actorSymbol(i);
        }
    }
/**/

