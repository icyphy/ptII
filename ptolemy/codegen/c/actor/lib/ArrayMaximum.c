/*** preinitBlock ***/
int $actorSymbol(i);
int $actorSymbol(indexValue);
$targetType(output) $actorSymbol(currentMax);
$targetType(output) $actorSymbol(temp);
/**/

/*** fireBlock ***/
$actorSymbol(indexValue) = 0;
$actorSymbol(currentMax) = $cgType(input)_get($ref(input), 0);

for ($actorSymbol(i) = 1; $actorSymbol(i) < $ref(input).payload.$cgType(input)->size; $actorSymbol(i)++) {
    $actorSymbol(temp) = $cgType(input)_get($ref(input), $actorSymbol(i));

    if ($actorSymbol(currentMax) < $actorSymbol(temp)) {
        $actorSymbol(indexValue) = $actorSymbol(i);
        $actorSymbol(currentMax) = $actorSymbol(temp);
    }
}

$ref(output) = $actorSymbol(currentMax);
$ref(index) = $actorSymbol(indexValue);
/**/
