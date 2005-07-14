/***initBlock***/
    $ref(output) = $val(init);
/**/

/***fireBlock***/
    if ($ref(reset)) {
    	$ref(output) = $val(init);
    } else {
    	$ref(output) += $ref(input);
    }
/**/
