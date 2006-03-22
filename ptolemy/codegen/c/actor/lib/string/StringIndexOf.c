/*** preinitBlock ***/
	int i, j;
/**/

/*** matchCaseFireBlock ($step)***/
	$ref(output) = -1;
	for (i = $ref(startIndex); i < strlen($ref(inText)); i += $step) {
		for (j = 0; j < strlen($ref(searchFor)); j++) {
			
		}
	}
/**/

/*** ignoreCaseFireBlock ($step)***/
    char * index;
    $ref(output) = -1;
	for (i = $ref(startIndex); i < strlen($ref(inText)); i += $step) {
	    if ((index = strstr($ref(inText) + i, $ref(searchFor))) != NULL) {
	        $ref(output) = index - $ref(inText);
	        break;
	    }
	}
/**/
