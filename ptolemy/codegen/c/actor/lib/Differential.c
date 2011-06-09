/***CommonPreinitBlock($type)***/
$type $actorSymbol(_currentInput) = 0;
$type $actorSymbol(_lastInput) = 0;
/**/

/***CommonFireBlock***/
$actorSymbol(_currentInput) = $ref(input);
if ($actorSymbol(_lastInput) == 0) {
    $ref(output) = $actorSymbol(_currentInput);
} else {
    $ref(output) = $actorSymbol(_currentInput) - $actorSymbol(_lastInput);
}
$actorSymbol(_lastInput) = $actorSymbol(_currentInput);
/**/



/***TokenPreinitBlock***/
Token $actorSymbol(_currentInput);
Token $actorSymbol(_lastInput);
boolean $actorSymbol(isFirst) = true;
/**/

/***TokenFireBlock***/
$actorSymbol(_currentInput) = $ref(input);
if ($actorSymbol(isFirst)) {
    $ref(output) = $actorSymbol(_currentInput);
    $actorSymbol(isFirst) = false;
} else {
    $ref(output) = $tokenFunc($actorSymbol(_currentInput)::subtract($actorSymbol(_lastInput)));
}
$actorSymbol(_lastInput) = $actorSymbol(_currentInput);
/**/
