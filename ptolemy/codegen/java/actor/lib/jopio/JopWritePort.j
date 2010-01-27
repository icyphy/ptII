/***preinitBlock***/
int _val = 0;
int _last_val;
/**/

/*** FireBlock($channel) ***/
_last_val = _val;
// no check on available input token here - so above assignment is useless
_last_val = $ref(input#$channel);
/**/

/*** PostFireBlock ***/
_val = _last_val;
com.jopdesign.sys.Native.wrMem(_val, $ref((Integer)portAddress));
/**/
