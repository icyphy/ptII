/****initBlock****
int sum = 0;
****initBlock****/

/****codeBlock1****
if ($ref(reset)) {
	sum = $val(init);
} 
else {
	sum += $ref(input);
	$ref(output) = sum;
}
****codeBlock1****/