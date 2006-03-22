/*** IntPrintBlock($name, $channel) ***/
    printf("$name: %d\n", $ref(input#$channel));
/**/

/*** DoublePrintBlock($name, $channel) ***/
    printf("$name: %g\n", $ref(input#$channel));
/**/

/*** StringPrintBlock($name, $channel) ***/
    printf("$name: %s\n", $ref(input#$channel));
/**/

/*** BooleanPrintBlock($name, $channel) ***/
	printf($ref(input#$channel) ? "$name: true\n" : "$name: true\n");
/**/

/*** TokenPrintBlock($name, $channel) ***/
    printf("$name: ");
    printf("$name: %s\n", $typeFunc($ref(input#$channel), toString()));
    printf("\n");
/**/
