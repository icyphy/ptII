/*** fireBlock ***/
	// FIXME: is this the proper way to free the allocated space?
	free($ref(output));

	$ref(output) = $typeFunc($ref(input), toExpression()).payload.String;
/**/
