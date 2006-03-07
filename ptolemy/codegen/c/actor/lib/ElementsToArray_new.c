/*** preinitBlock ***/
	int $actorSymbol(i);
/**/

/*** initBlock ($size)***/
    //$token(output) = $new(Array($size,0));
    $ref(output) = $Array($size);
/**/


/*** fireBlock($channel, $type) ***/
    //$token(output).payload.Array->elements[$channel] = $ref(input#$channel);

    for ($actorSymbol(i) = 0; $actorSymbol(i) < $width; $actorSymbol(i)++) {
	    $ref(output) = $ref(input#$actorSymbol(i));
    }
    
    // This should translate to:
    //for (i = 0; i < width; i++) {
	//    output = input[i];
    //}    
/**/


/*** primitiveToPrimitiveFireBlock($channel, $type) ***/
    //$token(output).payload.Array->elements[$channel] = $ref(input#$channel);
    $ref(output).payload.Array->elements[$channel] = $ref(input#$channel);
/**/

/*** primitiveToTokenFireBlock($channel, $type) ***/
    $token(output).payload.Array->elements[$channel] = $new($type($ref(input#$channel)));
/**/

/*** tokenFireBlock($channel, $type)***/
    $token(output).payload.Array->elements[$channel] = $token(input#$channel);
/**/
