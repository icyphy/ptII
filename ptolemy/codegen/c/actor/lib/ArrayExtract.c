/*** preinitBlock($elementType) ***/
int $actorSymbol(i);
$elementType $actorSymbol(firstInputElement);
boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
$actorSymbol(doDelete) = false;
/**/


/*** fireBlock($elementType) ***/
if ($actorSymbol(doDelete)) {
        $delete_$cgType(output)($ref(output));
}

$ref(output) = $new($cgType(output)($ref(outputArrayLength), 0));

// Get the first element from the input array.
$actorSymbol(firstInputElement) = $cgType(input)_get($ref(input), 0);

for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(destinationPosition); $actorSymbol(i)++) {
    $ref(output).payload.$cgType(output)->elements[$actorSymbol(i)] = $zero_$elementType($actorSymbol(firstInputElement));
}
for (; $actorSymbol(i) < $ref(destinationPosition) + $ref(extractLength); $actorSymbol(i)++) {
    $ref(output).payload.$cgType(output)->elements[$actorSymbol(i)] = $cgType(input)_get($ref(input), $ref(sourcePosition) + $actorSymbol(i) - $ref(destinationPosition));
}
for (; $actorSymbol(i) < $ref(outputArrayLength); $actorSymbol(i)++) {
    $ref(output).payload.$cgType(output)->elements[$actorSymbol(i)] = $zero_$elementType($actorSymbol(firstInputElement));
}
/**/

