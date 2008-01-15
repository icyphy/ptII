/***preinitBlock***/
char* $actorSymbol(string);
int $actorSymbol(length);
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
$actorSymbol(string) = $tokenFunc($ref(input#$channel)::toString()).payload.String;
$actorSymbol(length) = strlen($actorSymbol(string));
if ($actorSymbol(length) > 1 && $actorSymbol(string)[0] == '\"' && $actorSymbol(string)[$actorSymbol(length) - 1] == '\"') {
    $actorSymbol(string)[$actorSymbol(length) - 1] = '\0';
    printf("$name: %s\n", $actorSymbol(string) + 1);
} else {
    printf("$name: %s\n", $actorSymbol(string));
}
/**/
