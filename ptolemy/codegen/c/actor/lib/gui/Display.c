/***preinitBlock***/
	char* string;
	int length;
/**/

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
	printf($ref(input#$channel) ? "$name: true\n" : "$name: false\n");
/**/

/*** TokenPrintBlock($name, $channel) ***/
	string = $tokenFunc($ref(input#$channel)::toString()).payload.String;
	length = strlen(string);
	if (length > 1 && string[0] == '\"' && string[length - 1] == '\"') {
		string[length - 1] = '\0';
	    printf("$name: %s\n", string + 1);
	} else {
	    printf("$name: %s\n", string);
	}
/**/
