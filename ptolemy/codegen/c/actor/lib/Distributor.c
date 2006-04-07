/*** assignBlock($channel, $type) ***/
    $ref(output#$channel) = $ref(input, $channel);
/**/

/*** toTokenBlock($channel, $type) ***/
    $ref(output#$channel) = $new($type, $ref(input));
/**/

/*** IntToStringBlock($channel, $type) ***/
    $ref(output#$channel) = myItoa($ref(input));
/**/

/*** DoubleToStringBlock($channel, $type) ***/
    $ref(output#$channel) = myFtoa($ref(input));
/**/

/*** LongToStringBlock($channel, $type) ***/
    $ref(output#$channel) = myLtoa($ref(input));
/**/

/*** BooleanToStringBlock($channel, $type) ***/
    $ref(output#$channel) = myBtoa($ref(input));
/**/
