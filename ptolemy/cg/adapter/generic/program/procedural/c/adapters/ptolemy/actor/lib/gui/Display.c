/***preinitBlock***/
/**/

/*** IntPrintBlock($name, $channel) ***/
printf("%d\n", $get(input#$channel));
/**/

/*** DoublePrintBlock($name, $channel) ***/
printf("%f\n", $get(input#$channel));
/**/

/*** StringPrintBlock($name, $channel) ***/
printf("%s\n", $get(input#$channel));
/**/

/*** BooleanPrintBlock($name, $channel) ***/
printf("%d\n", $get(input#$channel));
/**/

/*** TokenPrintBlock($name, $channel) ***/
printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
/**/

