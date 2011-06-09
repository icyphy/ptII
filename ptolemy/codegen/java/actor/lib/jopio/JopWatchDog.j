/***preinitBlock***/
static com.jopdesign.io.SysDevice sys = com.jopdesign.io.IOFactory.getFactory().getSysDevice();
/**/

/*** BooleanOutput($channel) ***/
sys.wd = $ref(input#$channel) ? 1 : 0;
/**/

