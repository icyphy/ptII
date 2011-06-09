/***preinitBlock($width) ***/
//$targetType(output) $actorSymbol(temporaryOutput)[$width];
$targetType(output) $actorSymbol(temporaryOutput)[] = new $targetType(output)[$width];
int $actorSymbol(rowCount);
int $actorSymbol(min);
int $actorSymbol(i);
/**/

/*** fireBlock($width) ***/
$actorSymbol(rowCount) = ((matrix)($ref(input)).payload).row;
$actorSymbol(min) = $actorSymbol(rowCount) < $width ? $actorSymbol(rowCount) : $width;
/**/

/*** fireBlock2() ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(min); $actorSymbol(i)++) {
    $actorSymbol(temporaryOutput)[$actorSymbol(i)] = Matrix_get($ref(input), $actorSymbol(i), 0);
}
/**/

/*** fireBlock2($type) ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(min); $actorSymbol(i)++) {
    // FIXME: doubleValue() is wrong here
    $actorSymbol(temporaryOutput)[$actorSymbol(i)] = (($type)(Matrix_get($ref(input), $actorSymbol(i), 0).payload)).doubleValue();
}
/**/

/*** fireBlock3($channel) ***/
$ref(output#$channel) = $actorSymbol(temporaryOutput)[$channel];

/**/
