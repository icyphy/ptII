/*** fireBlock ***/
    if (($ref(select) >= 0) && ($ref(select) < $size(input))) {
    	// FIXME: variable channel index.
        $ref(output) = $ref(input#$ref(select));
    }
/**/

