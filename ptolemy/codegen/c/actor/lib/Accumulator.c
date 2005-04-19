/***initBlock*/
    $ref(output) = $val(init);
/**/

/***codeBlock1*/
if ($ref(reset)) {
	$ref(output) = $val(init);
} 
else {
	$ref(output) += $ref(input);
}
/**/