/*** fireBlock ***/
    if (($ref(control) >= 0) && ($ref(control) < $size(output))) {
    	// FIXME: variable channel index.
        $ref(output#$ref(control)) = $ref(input);
    }
/**/

