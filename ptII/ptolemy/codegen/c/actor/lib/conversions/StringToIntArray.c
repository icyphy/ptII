/*** preinitBlock ***/
int $actorSymbol(i);
int $actorSymbol(length);
boolean $actorSymbol(doDelete) = false;
Token $actorSymbol(intArray);
/**/

/*** fireBlock***/
if ($actorSymbol(doDelete)) {
    IntArray_delete($actorSymbol(intArray));
} else {
    $actorSymbol(doDelete) = true;
}

$actorSymbol(length) = strlen($ref(input));
$actorSymbol(intArray) = $new(IntArray($actorSymbol(length), 0));
for ($actorSymbol(i) = 0; $actorSymbol(i) < $actorSymbol(length); $actorSymbol(i)++) {
        printf("%d\n", $ref(input)[$actorSymbol(i)]);
        fflush(stdout);
    $actorSymbol(intArray).payload.IntArray->elements[$actorSymbol(i)] = (int) ($ref(input)[$actorSymbol(i)]);
}
$ref(output) = $actorSymbol(intArray);
/**/
