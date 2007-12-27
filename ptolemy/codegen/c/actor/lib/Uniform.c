/*** randomBlock ***/
#ifdef PT_NO_RANDOM_JAVA
$ref(output) = random() % ($ref(upperBound) - $ref(lowerBound)) + $ref(lowerBound);
#else
$ref(output) = (RandomSource_nextDouble(&$actorSymbol(seed)) * ($ref(upperBound) - $ref(lowerBound))) + $ref(lowerBound);
#endif
/**/
