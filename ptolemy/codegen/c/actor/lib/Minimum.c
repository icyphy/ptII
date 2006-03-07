/*** preinitBlock ***/
    int $actorSymbol(minimumTemp);
    int $actorSymbol(channelTemp);
/**/

/*** fireInitBlock***/
    $actorSymbol(minimumTemp) = $ref(input#0);
    $actorSymbol(channelTemp) = 0;
/**/

/*** findBlock(<arg>) ***/
    if ($ref(input#<arg>) < $actorSymbol(minimumTemp)) {
        $actorSymbol(minimumTemp) = $ref(input#<arg>);
        $actorSymbol(channelTemp) = <arg>;
    }
/**/

/*** sendBlock1(<arg>)***/
    $ref(minimumValue#<arg>) = $actorSymbol(minimumTemp);
/**/

/*** sendBlock2(<arg>)***/
    $ref(channelNumber#<arg>) = $actorSymbol(channelTemp);
/**/

