/***preinitBlock***/
/**/

/*** IntPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%d\n", $get(input#$channel));
}
/**/

/*** DoublePrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%f\n", $get(input#$channel));
}
/**/

/*** StringPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s\n", $get(input#$channel));
}
/**/

/*** BooleanPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%d\n", $get(input#$channel));
}
/**/

/*** TokenPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
}
/**/

