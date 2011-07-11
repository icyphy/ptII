/*** assignBlock($channel, $type) ***/
// there is no need for hasToken() because this actor
// only has one input port, so it should always be able
// to successfully read from the channel.
// Also, channel in get(input, channel) is actually
// used as the offset.
$put(output#$channel, $get(input, $channel));
/**/

/*** toTokenBlock($channel, $type) ***/
$put(output#$channel, $new($type, $get(input)));
/**/

/*** IntToStringBlock($channel, $type) ***/
$put(output#$channel, InttoString($get(input)));
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
