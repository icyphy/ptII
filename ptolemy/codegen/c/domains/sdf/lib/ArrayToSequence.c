/*** preinitBlock ***/
boolean $actorSymbol(enforceArrayLength);
int $actorSymbol(i);
/**/

/*** fireBlock ($type)***/
if ($ref(enforceArrayLength) && $ref(input).payload.$typeArray->size != $ref(arrayLength)) {
    printf("\nArrayToSequence fails\n");
    exit(-1);
}
for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input).payload.$typeArray->size; $actorSymbol(i)++) {
    $ref(output, $actorSymbol(i)) = $typeArray_get($ref(input), $actorSymbol(i));
}
/**/
