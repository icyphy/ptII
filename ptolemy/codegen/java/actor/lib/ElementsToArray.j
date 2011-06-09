/***preinitBlock ***/
Token $actorSymbol(valueArray);
/**/

/*** initBlock ***/
$actorSymbol(valueArray) = $new(Array($size(input), 0));
/**/

/*** fillArray($channel) ***/
((array)($actorSymbol(valueArray).payload)).elements[$channel] = $ref((Token) input#$channel);
/**/

/*** sendOutput ***/
$ref(output) = $actorSymbol(valueArray);
/**/

/*** wrapupBlock ***/
//If an array contains other arrays, calling this function
//on every array will cause problem (memory freed multiple times).
//Array_delete($actorSymbol(valueArray));
/**/
