/***preinitBlock($width) ***/
$targetType(output) $actorSymbol(temporaryOutput)[$width];
int $actorSymbol(rowCount);
int $actorSymbol(min);
int $actorSymbol(i);
/**/

/*** fireBlock($width) ***/
$actorSymbol(rowCount) = $ref(input).payload.Matrix->row;
$actorSymbol(min) = $actorSymbol(rowCount) < $width ? $actorSymbol(rowCount) : $width;
/**/

/*** fireBlock2() ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(min); $actorSymbol(i)++) {
    $actorSymbol(temporaryOutput)[$actorSymbol(i)] = Matrix_get($ref(input), $actorSymbol(i), 0);
}
/**/

/*** fireBlock2($type) ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(min); $actorSymbol(i)++) {
    $actorSymbol(temporaryOutput)[$actorSymbol(i)] = Matrix_get($ref(input), $actorSymbol(i), 0).payload.$type;
}
/**/

/*** fireBlock3($channel) ***/
$ref(output#$channel) = $actorSymbol(temporaryOutput)[$channel];

/**/
