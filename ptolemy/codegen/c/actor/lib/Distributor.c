/*** assignBlock($channel, $type) ***/
    $ref(output#$channel) = $ref(input, $channel);
/**/

/*** toTokenBlock($channel, $type) ***/
    $ref(output#$channel) = $new($type, $ref(input));
/**/

/*** IntToStringBlock($channel, $type) ***/
    $ref(output#$channel) = InttoString($ref(input));
/**/

/*** DoubleToStringBlock($channel, $type) ***/
    $ref(output#$channel) = DoubletoString($ref(input));
/**/

/*** LongToStringBlock($channel, $type) ***/
    $ref(output#$channel) = LongtoString($ref(input));
/**/

/*** BooleanToStringBlock($channel, $type) ***/
    $ref(output#$channel) = BooleantoString($ref(input));
/**/
