/*** preinitBlock ***/
int $actorSymbol(maximumTemp);
int $actorSymbol(channelTemp);
int $actorSymbol(channel);
/**/

/*** fireInitBlock($width)***/
for ($actorSymbol(channel) = 0; $actorSymbol(channel) < $width; $actorSymbol(channel)++) {
	if (hasToken($actorSymbol(channel)) {
		$actorSymbol(maximumTemp) = $get(input#$actorSymbol(channel));
		$actorSymbol(channelTemp) = $actorSymbol(channel);
		break;
	}
}
/**/

/*** findBlock($arg) ***/
if (hasToken($arg)) {
	if ($get(input#$arg) > $actorSymbol(maximumTemp)) {
   		$actorSymbol(maximumTemp) = $get(input#$arg);
    	$actorSymbol(channelTemp) = $arg;
	}
}
/**/

/*** sendBlock1($arg)***/
$put(maximumValue#$arg, $actorSymbol(maximumTemp));
/**/

/*** sendBlock2($arg)***/
$put(channelNumber#$arg, $actorSymbol(channelTemp));
/**/
