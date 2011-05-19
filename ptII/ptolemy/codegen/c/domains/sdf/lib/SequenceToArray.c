/*** preinitBlock ***/
int $actorSymbol(i);
Token $actorSymbol(valueArray);
/**/

/*** fireBlock ***/
$actorSymbol(valueArray) = $new($cgType(output)($ref(arrayLength), 0));
for ($actorSymbol(i) = 0;
     $actorSymbol(i) < $ref(arrayLength);
     $actorSymbol(i)++) {
    $cgType(output)_set($actorSymbol(valueArray),
            $actorSymbol(i),
            $ref(input, $actorSymbol(i)));
}
$ref(output) =
        $actorSymbol(valueArray);
/**/
