/***preinitBlock***/
int $actorSymbol(_val) = 0;
int $actorSymbol(_last_val);
/**/

/*** FireBlock($channel) ***/
$actorSymbol(_last_val) = $actorSymbol(_val);
// no check on available input token here - so above assignment is useless
$actorSymbol(_last_val) = $ref(input#$channel);
/**/

/*** PostFireBlock ***/
$actorSymbol(_val) = $actorSymbol(_last_val);
com.jopdesign.sys.Native.wrMem($actorSymbol(_val), $ref((Integer)portAddress));
/**/
