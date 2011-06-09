/*** preinitBlock ***/
int $actorSymbol(maximumTemp);
int $actorSymbol(channelTemp);
/**/

/*** fireInitBlock($channel)***/
if ($hasToken(input#$channel)) {
        $actorSymbol(maximumTemp) = $get(input#$channel);
        $actorSymbol(channelTemp) = $channel;
        break;
}
/**/

/*** findBlock($arg) ***/
if ($hasToken(input#$arg)) {
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
