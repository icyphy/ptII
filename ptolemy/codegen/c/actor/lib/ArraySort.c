/*** sharedBlock ***/
Token ArraySort_sort(Token array) {
	int i;
	Token result = $new(Array(array.payload.Array->size, 0));
	Token element;
	for (i = 0; i < array.payload.Array->size; i++) {
		element = Array_get(array, i);
		
	}
	return result;
}

Token ArraySort_sortDescending(Token array) {
	Token result = $new(Array(array.payload.Array->size, 0));
	return result;
}
/**/


/*** preinitBlock ***/
Token result;
/**/

/*** fireBlock ***/
    if ($ref(input).payload.Array->size == 0) {
        $ref(output) = $ref(input);
    } else {
	    ArrayToken result = null;
	
        if ($val(ascending)) {
            result = UtilityFunctions.sort($ref(input));
        } else {
            result = UtilityFunctions.sortDescending($ref(input));
        }
	
		
	    if (!allowDuplicatesValue) {
	        // Strip out duplicates.
	        ArrayList list = new ArrayList();
	        Token previous = result.getElement(0);
	        list.add(previous);
	
	        for (int i = 1; i < result.length(); i++) {
	            Token next = result.getElement(i);
	
	            if (!next.isEqualTo(previous).booleanValue()) {
	                list.add(next);
	                previous = next;
	            }
	        }
	
	        // Dummy array to give the run-time type to toArray().
	        Token[] dummy = new Token[0];
	        result = new ArrayToken((Token[]) list.toArray(dummy));
	    }
	
	    output.send(0, result);
	}
/**/

