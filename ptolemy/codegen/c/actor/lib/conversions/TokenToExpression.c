/*** TokenFireBlock ***/
	// FIXME: is this the proper way to free the allocated space?
	//free($ref(output));

	$ref(output) = $tokenFunc($ref(input)::toString()).payload.String;
/**/

/*** FireBlock($type) ***/
	$ref(output) = $typetoString($ref(input));
/**/