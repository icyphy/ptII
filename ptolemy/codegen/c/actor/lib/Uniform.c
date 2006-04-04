/*** randomBlock ***/
    $ref(output) = (RandomSource_nextDouble(&$actorSymbol(seed)) * ($val(upperBound) - $val(lowerBound))) + $val(lowerBound);
/**/
