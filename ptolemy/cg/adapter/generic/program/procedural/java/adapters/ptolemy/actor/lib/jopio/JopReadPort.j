/***preinitBlock***/
boolean $actorSymbol(firstFire) = true;
/**/

/*** FireBlock($channel) ***/
if ($actorSymbol(firstFire)) {
    // read the value
    $put(output, com.jopdesign.sys.Native.rdMem($param(portAddress)));
    $actorSymbol(firstFire) = false;
}
// do we have to consume the token on a following fire?
/**/

/*** postfireBlock ***/
$actorSymbol(firstFire) = true;
/**/
