/***initBlock*/
int sum = 0;
/**/

/***codeBlock1*/
if ($ref(reset)) {
	sum = $val(init);
} 
else {
	sum += $ref(input);
	$ref(output) = sum;
}
/**/