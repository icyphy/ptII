/***preinitBlock***/
boolean _firstFire = true;
/**/

/*** FireBlock($channel) ***/
if (_firstFire) {
    // read the value
    $ref(output) = com.jopdesign.sys.Native.rdMem($ref((Integer)portAddress));
    _firstFire = false;
}
/**/

/*** PostFireBlock ***/
_firstFire = true;
/**/
