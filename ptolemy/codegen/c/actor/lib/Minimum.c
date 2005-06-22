/*** initMin ***/
    $ref(minimumValue) = $ref(input#0);
/**/

/*** initChannelNum ***/
    $ref(channelNumber) = -1;
/**/

/*** compareBlock(i) ***/
    $ref(minimumValue) = $ref(input#i) < $ref(minimumValue) ? $ref(input#i) : $ref(minimumValue);
/**/

