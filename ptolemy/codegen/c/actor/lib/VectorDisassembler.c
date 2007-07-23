/***preinitBlock($width) ***/
$targetType(output) $actorSymbol(temporaryOutput)[$width];
/**/

/*** fireBlock($width) ***/
int rowCount = $ref(input).payload.Matrix->row;
int min = rowCount < $width ? rowCount : $width;
int i;
/**/

/*** fireBlock2() ***/
for (i = 0; i < min; i++) {
    $actorSymbol(temporaryOutput)[i] = Matrix_get($ref(input), i, 0);
}
/**/

/*** fireBlock2($type) ***/
for (i = 0; i < min; i++) {
    $actorSymbol(temporaryOutput)[i] = Matrix_get($ref(input), i, 0).payload.$type;
}
/**/

/*** fireBlock3($channel) ***/
$ref(output#$channel) = $actorSymbol(temporaryOutput)[$channel];

/**/
