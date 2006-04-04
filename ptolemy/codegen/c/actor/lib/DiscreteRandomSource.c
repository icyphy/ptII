/*** sharedBlock2 ***/
	double DiscreteRandomSource_rand(double* seed, Token pmf, Token values) {
		int i;
	    double randomValue;
	    double cdf = 0.0;
	    
	    // Generate a double between 0 and 1, uniformly distributed.
	    randomValue = RandomSource_nextDouble(seed);

	    for (i = 0; i < pmf.payload.Array->size; i++) {
	        cdf += Array_get(pmf, i).payload.Double;

	        if (randomValue <= cdf) {
	            return Array_get(values, i).payload.Double;
	        }
	    }

	    // We shouldn't get here, but if we do, we output the last value.
	    return Array_get(values, pmf.payload.Array->size - 1).payload.Double;
	}
/**/


/*** randomBlock ***/
	$ref(output) = DiscreteRandomSource_rand(&$actorSymbol(seed), $ref(pmf), $ref(values));
/**/

