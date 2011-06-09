/***initBlock***/
// FIXME: the code template for DiscreteClock is _extremely_ incomplete.
// First, this version will only produce an output event when it receives
// an event on its input. 
// Second, the value of the output will always be 0.
$put(output#0, 0);
/**/

/***fireBlock***/
$put(output#0, $get(trigger#0));
/**/
