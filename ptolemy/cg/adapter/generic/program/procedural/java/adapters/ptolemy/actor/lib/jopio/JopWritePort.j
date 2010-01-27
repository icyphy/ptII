/***preinitBlock***/
int $actorSymbol(val) = 0;
int $actorSymbol(lastVal);
/**/

/*** FireBlock($channel) ***/
$actorSymbol(lastVal) = $actorSymbol(val);
// no check on available input token here - so above assignment is useless
$actorSymbol(lastVal) = $get(input#$channel);
/**/

/*** postfireBlock ***/
$actorSymbol(val) = $actorSymbol(lastVal);
com.jopdesign.sys.Native.wrMem($actorSymbol(val), $param(portAddress));
/**/
