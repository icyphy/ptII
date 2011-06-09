/*** preinitBlock ***/
int $actorSymbol(i);
int $actorSymbol(j);
int $actorSymbol(searchForLength);
/**/

/*** ignoreCaseFireBlock ($step)***/
$actorSymbol(searchForLength) = strlen($ref(searchFor));
$ref(output) = -1;

for ($actorSymbol(i) = $ref(startIndex); $actorSymbol(i) < 1 + strlen($ref(inText)) - $actorSymbol(searchForLength) - $ref(startIndex); $actorSymbol(i) += $step) {
    // FIXME: strncasecmp() is a GNU extension.
    if (strncasecmp($ref(inText) + $actorSymbol(i), $ref(searchFor), $actorSymbol(searchForLength)) == 0) {
        $ref(output) = $actorSymbol(i);
        break;
    }
}
/**/

/*** matchCaseFireBlock ($step)***/
$actorSymbol(searchForLength) = strlen($ref(searchFor));
$ref(output) = -1;

for ($actorSymbol(i) = $ref(startIndex); $actorSymbol(i) < 1 + strlen($ref(inText)) - $actorSymbol(searchForLength) - $ref(startIndex); $actorSymbol(i) += $step) {
    if (strncmp($ref(inText) + $actorSymbol(i), $ref(searchFor), $actorSymbol(searchForLength)) == 0) {
        $ref(output) = $actorSymbol(i);
        break;
    }
}
/**/
