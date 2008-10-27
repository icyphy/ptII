/*** CommonPreinitBlock($type) ***/
int $actorSymbol(count);
double $actorSymbol(factor);
int $actorSymbol(mostRecent);
$type $actorSymbol(sum);
$type* $actorSymbol(data);
int $actorSymbol(typeSize) = sizeof($type);
/**/

/*** initBlock ***/
$actorSymbol(count) = 0;
$actorSymbol(mostRecent) = $val(maxPastInputsToAverage);
$actorSymbol(data) = malloc($val(maxPastInputsToAverage) * $actorSymbol(typeSize));
/**/

/***fireBlock***/
int i;
if ($actorSymbol(mostRecent) <= 0) {
    $actorSymbol(mostRecent) = $val(maxPastInputsToAverage) - 1;
} else {
   --$actorSymbol(mostRecent);
}
$actorSymbol(data)[$actorSymbol(mostRecent)] = $ref(input);
if ($actorSymbol(count) < $val(maxPastInputsToAverage)) {
    $actorSymbol(count)++;
    $actorSymbol(factor) = 1.0/$actorSymbol(count);
}
$actorSymbol(sum) = $actorSymbol(data)[$actorSymbol(mostRecent)];
for (i = 1; i < $actorSymbol(count); i++) {
    int dataIndex = ($actorSymbol(mostRecent) + i ) % ($val(maxPastInputsToAverage));
    $actorSymbol(sum) += $actorSymbol(data)[dataIndex];

}
$ref(output) = $actorSymbol(factor) * $actorSymbol(sum);
/**/
