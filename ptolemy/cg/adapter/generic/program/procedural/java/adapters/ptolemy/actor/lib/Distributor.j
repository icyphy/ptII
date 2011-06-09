/*** assignBlock($channel, $type) ***/
$put(output#$channel, $get(input, $channel));
/**/

/*** toTokenBlock($channel, $type) ***/
$put(output#$channel, $new($type, $get(input)));
/**/

/*** IntToStringBlock($channel, $type) ***/
$put(output#$channel, IntegertoString($get(input)));
/**/

/*** DoubleToStringBlock($channel, $type) ***/
$put(output#$channel, DoubletoString($get(input)));
/**/

/*** LongToStringBlock($channel, $type) ***/
$put(output#$channel, LongtoString($get(input)));
/**/

/*** BooleanToStringBlock($channel, $type) ***/
$put(output#$channel, BooleantoString($get(input)));
/**/
