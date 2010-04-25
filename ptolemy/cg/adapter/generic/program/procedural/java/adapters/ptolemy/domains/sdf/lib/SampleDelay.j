/*** initTokens($offset) ***/
// SampleDelayInitTokens
$put(output, $offset, $val(initialOutputs, $offset));
/**/

/***preinitBlock($type)***/
//$type $actorSymbol(lastVal);
/**/

/***fireBlock***/
//DOLLARput(output, DOLLARactorSymbol(lastVal));
//DOLLARactorSymbol(lastVal) = DOLLARget(input);
$put(output, $get(input));
/**/
