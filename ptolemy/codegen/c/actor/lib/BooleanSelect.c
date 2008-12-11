/*** fireBlock ***/
if ($ref(control)) {
	$sendTrueInputs()
} else {
	$sendFalseInputs()
}
/**/

/*** trueBlock($channel) ***/
    $ref(output#$channel) = $ref(trueInput#$channel);
/**/

/*** falseBlock($channel) ***/
    $ref(output#$channel) = $ref(falseInput#$channel);
/**/
