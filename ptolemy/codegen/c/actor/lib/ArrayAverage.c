/*** preinitBlock ***/
int $actorSymbol(i);
$targetType(output) $actorSymbol(sum);
/**/

/*** fireBlock ***/
$actorSymbol(sum) = $cgType(input)_get($ref(input), 0);
for ($actorSymbol(i) = 1; $actorSymbol(i) < $ref(input).payload.$cgType(input)->size; $actorSymbol(i)++) {
        $actorSymbol(sum) = $add_$cgType(output)_$cgType(output)($actorSymbol(sum), $cgType(input)_get($ref(input), $actorSymbol(i)));
}
$ref(output) = $divide_$cgType(output)_Int($actorSymbol(sum), $ref(input).payload.$cgType(input)->size);
/**/

