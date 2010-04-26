/***preinitBlock ***/
Token $actorSymbol(valueArray);
/**/

/*** initBlock ***/
$actorSymbol(valueArray) = $new(Array($size(input), 0));
/**/

/*** fillArray($channel) ***/
((Array)($actorSymbol(valueArray).payload)).elements[$channel] = $convert_$cgType(input)_Token($get(input#$channel));
/**/

/*** sendOutput ***/
$put(output, $actorSymbol(valueArray));
/**/

/*** wrapupBlock ***/
//If an array contains other arrays, calling this function
//on every array will cause problem (memory freed multiple times).
//Array_delete($actorSymbol(valueArray));
/**/
