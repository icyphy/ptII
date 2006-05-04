/*** preinitBlock ***/
int $actorSymbol(i);
double $actorSymbol(sum);
/**/

/*** fireBlock ***/
$actorSymbol(sum) = Array_get($ref(input), 0).payload.$cgType(output);
for ($actorSymbol(i) = 1; $actorSymbol(i) < $ref(input).payload.Array->size; $actorSymbol(i)++) {
	$actorSymbol(sum) += Array_get($ref(input), $actorSymbol(i)).payload.$cgType(output);
}
$ref(output) = $actorSymbol(sum) / $ref(input).payload.Array->size;
/**/

