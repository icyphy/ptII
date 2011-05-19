/***preinitBlock ***/
Token $actorSymbol(valueArray);
/**/

/*** initBlock ***/
$actorSymbol(valueArray) = $new($cgType(output)($size(input), 0));
/**/

/*** fillArray($channel) ***/
$actorSymbol(valueArray).payload.$cgType(output)->elements[$channel] = $ref(input#$channel);
/**/

/*** sendOutput ***/
$ref(output) = $actorSymbol(valueArray);
/**/

/*** wrapupBlock ***/
//If an array contains other arrays, calling this function
//on every array will cause problem (memory freed multiple times).
//Array_delete($actorSymbol(valueArray));
/**/
