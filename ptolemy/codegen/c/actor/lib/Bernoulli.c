/***randomBlock***/
    if (rand_r(&$actorSymbol(seed)) < $val(trueProbability) * RAND_MAX) {
        $ref(output) = 1;
    } else {
        $ref(output) = 0;	
    } 	
/**/