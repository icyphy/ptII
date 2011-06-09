/***preinitBlock***/
boolean $actorSymbol(_firstFire) = true;
/**/

/*** FireBlock($channel) ***/
if ($actorSymbol(_firstFire)) {
    // read the value
    $ref(output) = com.jopdesign.sys.Native.rdMem($ref((Integer)portAddress));
    $actorSymbol(_firstFire) = false;
}
/**/

/*** postfireBlock ***/
$actorSymbol(_firstFire) = true;
/**/
