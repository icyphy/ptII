/*** preinitBlock ***/
int $actorSymbol(i);
boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
$actorSymbol(doDelete) = false;
/**/


/*** fireBlock ***/
if ($actorSymbol(doDelete)) {
    Array_delete($ref(output));
}

$ref(output) = Array_new($ref(outputArrayLength), 0);

for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(destinationPosition); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = $tokenFunc(Array_get($ref(input), 0)::zero());
}
for (; $actorSymbol(i) < $ref(destinationPosition) + $ref(extractLength); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = Array_get($ref(input), $ref(sourcePosition) + $actorSymbol(i) - $ref(destinationPosition));
}
for (; $actorSymbol(i) < $ref(outputArrayLength); $actorSymbol(i)++) {
    $ref(output).payload.Array->elements[$actorSymbol(i)] = $tokenFunc(Array_get($ref(input), 0)::zero());
}
/**/

