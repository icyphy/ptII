/*** preinitBlock ***/
int $actorSymbol(i);
/**/

/*** fireBlock ($type1, $type2)***/
// Assume both $ref(input1) and $ref(input2) are array type.

$ref(output) = 0;

for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input1).payload.$cgType(input1)->size; $actorSymbol(i)++) {
    $ref(output) += $multiply_$type1_$type2($cgType(input1)_get($ref(input1), $actorSymbol(i)), $cgType(input2)_get($ref(input2), $actorSymbol(i)));
}
/**/

