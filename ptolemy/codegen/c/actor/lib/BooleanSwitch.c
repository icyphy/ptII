/*** fireBlock ***/
if ($ref(control)) {
	$generateTrueOutputs()
} else {
	$generateFalseOutputs()
}
/**/

/*** trueBlock($channel) ***/
$ref(trueOutput#$channel) = $ref(input#$channel);
/**/

/*** falseBlock($channel) ***/
$ref(falseOutput#$channel) = $ref(input#$channel);
/**/
