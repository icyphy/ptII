/*** preinitBlock($type) ***/
$type $actorSymbol(minimumTemp);
int $actorSymbol(channelTemp);
/**/

/*** fireInitBlock***/
$actorSymbol(minimumTemp) = $get(input#0);
$actorSymbol(channelTemp) = 0;
/**/

/*** findBlock($arg) ***/
if ($get(input#$arg) < $actorSymbol(minimumTemp)) {
    $actorSymbol(minimumTemp) = $get(input#$arg);
    $actorSymbol(channelTemp) = $arg;
}
/**/

/*** sendBlock1($arg)***/
$put(minimumValue#$arg, $actorSymbol(minimumTemp));
/**/

/*** sendBlock2($arg)***/
$put(channelNumber#$arg, $actorSymbol(channelTemp));
/**/

