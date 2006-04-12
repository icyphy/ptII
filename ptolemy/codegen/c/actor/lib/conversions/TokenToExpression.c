/*** fireBlock ***/
	// FIXME: is this the proper way to free the allocated space?
	free($ref(output));

	$ref(output) = $tokenFunc($ref(input)::toExpression()).payload.String;
/**/
