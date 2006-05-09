/***preinitBlock ***/
	Token $actorSymbol(valueArray);
/**/

/*** initBlock ***/
    $actorSymbol(valueArray) = $new(Array($size(input), 0));
/**/

/*** fillArray($channel) ***/
    $actorSymbol(valueArray).payload.Array->elements[$channel] = $ref((Token) input#$channel);
/**/

/*** sendOutput ***/
	$ref(output) = $actorSymbol(valueArray);
/**/

/*** wrapupBlock ***/
	Array_delete($actorSymbol(valueArray));
/**/
