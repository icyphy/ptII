/*** preinitBlock ***/
int $actorSymbol(length);
int $actorSymbol(i);
//boolean $actorSymbol(doDelete);
/**/

/*** initBlock ***/
//$actorSymbol(doDelete) = false;
/**/

/*** fireBlock ***/
// FIXME: how should we do the freeing?
//if ($actorSymbol(doDelete)) {
//    $$cgType(output)_delete($ref(output));
//}
$actorSymbol(length) = 0;
/**/


/*** getTotalLength($channel) ***/
$actorSymbol(length) += $ref(input#$channel).payload.$cgType(input)->size;
/**/

/*** allocNewArray ***/
$ref(output) = $new($cgType(output)($actorSymbol(length), 0));
$actorSymbol(length) = 0;
/**/

/*** fillArray($channel, $elementType) ***/
for ($actorSymbol(i) = 0; $actorSymbol(i) < $ref(input#$channel).payload.Array->size; $actorSymbol(i)++) {
        $cgType(output)_set($ref(output), $actorSymbol(length),
    $clone_$elementType($cgType(input)_get($ref(input#$channel), $actorSymbol(i))));
    $actorSymbol(length)++;
}
/**/

/***doDelete***/
//$actorSymbol(doDelete) = true;
/**/

