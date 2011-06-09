/***preinitBlock***/
static com.jopdesign.io.SysDevice $actorSymbol(sys) = com.jopdesign.io.IOFactory.getFactory().getSysDevice();
int $actorSymbol(val) = 0;
/**/

/*** FireBlock($channel) ***/
$actorSymbol(val) = $get(input#$channel) ? 1 : 0;
/**/

/*** postfireBlock ***/
$actorSymbol(sys).wd = $actorSymbol(val);
/**/
