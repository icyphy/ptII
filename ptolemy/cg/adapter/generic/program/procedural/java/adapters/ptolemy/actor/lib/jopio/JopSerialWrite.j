/***preinitBlock***/
int $actorSymbol(val) = 0;
int $actorSymbol(lastVal);
static com.jopdesign.io.SerialPort $actorSymbol(ser) = com.jopdesign.io.IOFactory.getFactory().getSerialPort();
/**/

/*** FireBlock($channel) ***/
$actorSymbol(lastVal) = $actorSymbol(val);
// no check on available input token here - so above assignment is useless
$actorSymbol(lastVal) = $get(input#$channel);
/**/

/*** postfireBlock ***/
$actorSymbol(val) = $actorSymbol(lastVal);
if (($actorSymbol(ser).status & com.jopdesign.io.SerialPort.MASK_TDRE)!=0) {
        $actorSymbol(ser).data = $actorSymbol(val);
}
/**/
