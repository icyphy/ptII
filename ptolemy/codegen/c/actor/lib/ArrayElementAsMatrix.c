/*** TokenFireBlock ***/
$ref(output) =
        Array_get($ref(input),
                $ref(x) * $ref(xOffset) + $ref(y) * $ref(yOffset));
/**/

/*** PrimitiveFireBlock ***/
$ref(output) =
        Array_get($ref(input),
                $ref(x) * $ref(xOffset) + $ref(y) * $ref(yOffset)).payload.$cgType(output);
/**/
