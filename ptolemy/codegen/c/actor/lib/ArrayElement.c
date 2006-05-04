/*** TokenFireBlock ***/
	$ref(output) = Array_get($ref(input), $ref(index));
/**/

/*** PrimitiveFireBlock ***/
	$ref(output) = Array_get($ref(input), $ref(index)).payload.$cgType(output);
/**/
