/***FireBlock***/
	// primitive is commutative.
    $ref(output) = $val(factor) * $ref(input);
/**/

/***TokenFireBlock***/
	if ($ref(scaleOnLeft)) {
	    $ref(output) = Scale_scaleOnLeft($ref(input), (double) $val(factor));
	} else {
	    $ref(output) = Scale_scaleOnRight($ref(input), (double) $val(factor));
	}	
/**/

/***sharedScaleOnLeftBlock***/
Token Scale_scaleOnLeft(Token input, double factor) {
	int i;
	Token result;

    if (input.type == TYPE_Array) {
    	result = $new(Array(input.payload.Array->size, 0));

        for (i = 0; i < input.payload.Array->size; i++) {
            result.payload.Array->elements[i] = Scale_scaleOnLeft(Array_get(input, i), factor);
        }

        return result;
    } else {
        return $tokenFunc($new(Double(factor))::multiply(input));
    }
}
/**/

/***sharedScaleOnRightBlock***/
Token Scale_scaleOnRight(Token input, double factor) {
	int i;
	Token result;

    if (input.type == TYPE_Array) {
    	result = $new(Array(input.payload.Array->size, 0));

        for (i = 0; i < input.payload.Array->size; i++) {
            result.payload.Array->elements[i] = Scale_scaleOnRight(Array_get(input, i), factor);
        }

        return result;
    } else {
        return $tokenFunc(input::multiply($new(Double(factor))));
    }
}
/**/