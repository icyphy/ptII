/*** randomBlock ***/
#ifdef PT_NO_RANDOM_JAVA
$ref(output) = random() % ($val(upperBound) - $val(lowerBound)) + $val(lowerBound);
#else
$ref(output) = (RandomSource_nextDouble(&$actorSymbol(seed)) * ($val(upperBound) - $val(lowerBound))) + $val(lowerBound);
#endif
/**/
