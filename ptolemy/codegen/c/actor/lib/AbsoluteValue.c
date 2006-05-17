/***fireBlock***/
// Cannot use abs() because abs() does not work for all types.
//$ref(output) = abs($ref(input));
$ref(output) = $ref(input) < 0.0 ? -$ref(input) : $ref(input);
/**/
