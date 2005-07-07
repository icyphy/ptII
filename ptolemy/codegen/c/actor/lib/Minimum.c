/*** preinitBlock ***/
    int $actorSymbol(i);
/**/

/*** initMin ***/
    $ref(minimumValue) = $ref(input#0);
/**/

/*** initChannelNum ***/
    $ref(channelNumber) = -1;
/**/

/*** fireBlock(<arg>) ***/
    for ($actorSymbol(i) = 0; $actorSymbol(i) < <arg>; $actorSymbol(i)++) {
        if ($ref(input#$actorSymbol(i)) < $ref(minimumValue)) {
            $ref(minimumValue) =  $ref(input#$actorSymbol(i));
            $ref(channelNumber) = $actorSymbol(i);
        }
    }
/**/

/*** initBlock ***//**/

/*** wrapupBlock ***//**/
