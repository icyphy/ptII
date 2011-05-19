/***preinitBlock***/
/**/

/*** IntegerPrintBlock($name, $channel) ***/
System.out.println("$name: " + $ref(input#$channel));
/**/

/*** DoublePrintBlock($name, $channel) ***/
System.out.println("$name: " + $ref(input#$channel));
/**/

/*** StringPrintBlock($name, $channel) ***/
System.out.println("$name: " + $ref(input#$channel));
/**/

/*** BooleanPrintBlock($name, $channel) ***/
System.out.println("$name: " + $ref(input#$channel));

/**/

/*** TokenPrintBlock($name, $channel) ***/
System.out.println("$name: " + $tokenFunc($ref(input#$channel)::toString()).payload);
/**/
