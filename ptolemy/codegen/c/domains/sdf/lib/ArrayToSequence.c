/*** preinitBlock ***/
boolean $actorSymbol(enforceArrayLength);
int $actorSymbol(i);
/**/

/*** TokenFireBlock ***/
if ($ref(enforceArrayLength) && $ref(input).payload.Array->size != $ref(arrayLength)) {
    printf("\nArrayToSequence fails\n");
    exit(-1);
}
for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
    $ref(output, $actorSymbol(i)) = Array_get($ref(input), $actorSymbol(i));
}   
/**/

/*** fireBlock ($type)***/
if ($ref(enforceArrayLength) && $ref(input).payload.Array->size != $ref(arrayLength)) {
    printf("\nArrayToSequence fails\n");
    exit(-1);
}
for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
    $ref(output, $actorSymbol(i)) = Array_get($ref(input), $actorSymbol(i)).payload.$type;
}        
/**/
