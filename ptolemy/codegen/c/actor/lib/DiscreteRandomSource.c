/*** DiscreteRandomSource_sharedBlock***/
double RandomSource_nextDouble(double* seed);

double DiscreteRandomSource_$cgType(output)_rand(double* seed, Token pmf, Token values) {
    int i;
    double randomValue;
    double cdf = 0.0;

    // Generate a double between 0 and 1, uniformly distributed.
    randomValue = RandomSource_nextDouble(seed);

    for (i = 0; i < pmf.payload.$cgType(output)Array->size; i++) {
        cdf += DoubleArray_get(pmf, i);

        if (randomValue <= cdf) {
            return $cgType(output)Array_get(values, i);
        }
    }

    // We shouldn't get to here, but if we do, we output the last value.
    return $cgType(output)Array_get(values, pmf.payload.$cgType(output)Array->size - 1);
}
/**/

/*** randomBlock ***/
$ref(output) = DiscreteRandomSource_$cgType(output)_rand(&$actorSymbol(seed), $ref(pmf), $ref(values));
/**/

