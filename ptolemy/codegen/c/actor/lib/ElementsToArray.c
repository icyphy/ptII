/***preinitBlock ***/
	Token $actorSymbol(valueArray);
/**/

/*** initBlock ***/
    $actorSymbol(valueArray) = $new(Array($size(input), 0));
/**/

/*** primitiveFireBlock($channel, $type) ***/
    $actorSymbol(valueArray).payload.Array->elements[$channel] = $new($type($ref(input#$channel)));
	$ref(output) = $actorSymbol(valueArray);
/**/

/*** tokenFireBlock($channel, $type)***/
	$actorSymbol(valueArray).payload.Array->elements[$channel] = ($ref(input#$channel).type == TYPE_$type) ?
	    $ref(input#$channel) : 
	    $typeFunc(TYPE_$type::convert($ref(input#$channel)));	
	    
	$ref(output) = $actorSymbol(valueArray);
/**/

/*** wrapupBlock ***/
	Array_delete($actorSymbol(valueArray));
/**/
