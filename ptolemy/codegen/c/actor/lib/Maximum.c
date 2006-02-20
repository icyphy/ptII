/*** preinitBlock ***/
    int $actorSymbol(maximumTemp);
    int $actorSymbol(channelTemp);
/**/

/*** fireInitBlock***/
    $actorSymbol(maximumTemp) = $ref(input#0);
    $actorSymbol(channelTemp) = 0;
/**/

/*** fireBlock(<arg>) ***/
    if ($ref(input#<arg>) > $actorSymbol(maximumTemp)) {
        $actorSymbol(maximumTemp) = $ref(input#<arg>);
        $actorSymbol(channelTemp) = <arg>;
    }
/**/

/*** sendBlock1(<arg>)***/
    $ref(maximumValue#<arg>) = $actorSymbol(maximumTemp);
/**/

/*** sendBlock2(<arg>)***/
    $ref(channelNumber#<arg>) = $actorSymbol(channelTemp);
/**/
