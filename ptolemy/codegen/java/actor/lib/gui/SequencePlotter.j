/***preinitBlock***/
/**/

/*** IntPrintBlock($name, $channel) ***/
System.out.println("$name: " + $get(input#$channel));
/**/

/*** DoublePrintBlock($name, $channel) ***/
System.out.println("$name: " + $get(input#$channel));
/**/

/*** StringPrintBlock($name, $channel) ***/
System.out.println("$name: " + $get(input#$channel));
/**/

/*** BooleanPrintBlock($name, $channel) ***/
System.out.println("$name: " + $get(input#$channel));

/**/

/*** TokenPrintBlock($name, $channel) ***/
System.out.println("$name: " + $tokenFunc($get(input#$channel)::toString()).payload);
/**/
