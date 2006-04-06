/*** assignBlock($channel, $type) ***/
    $ref(output#$channel) = $ref(input, $channel);
/**/

/*** toTokenBlock($channel, $type) ***/
    $ref(output#$channel) = $new($type, $ref(input));
/**/

/*** IntToStringBlock($channel, $type) ***/
    $ref(output#$channel) = itoa($ref(input));
/**/

/*** DoubleToStringBlock($channel, $type) ***/
    $ref(output#$channel) = ftoa($ref(input));
/**/

/*** LongToStringBlock($channel, $type) ***/
    $ref(output#$channel) = ltoa($ref(input));
/**/

/*** BooleanToStringBlock($channel, $type) ***/
    $ref(output#$channel) = btoa($ref(input));
/**/
