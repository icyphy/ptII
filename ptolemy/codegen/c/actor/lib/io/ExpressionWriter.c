/***preinitBlock***/
boolean $actorSymbol(overwriteOK) = true;
FILE * $actorSymbol(filePtr);
char* $actorSymbol(expression);
/**/

/***writeLine***/
$actorSymbol(expression) = $tokenFunc($ref(input)::toString()).payload.String;
if ($actorSymbol(overwriteOK)) {
    fprintf($actorSymbol(filePtr), "%s\n", $actorSymbol(expression));
}
free($actorSymbol(expression));
/**/
