/***randomBlock***/
   /* Note that this code will generate different random numbers than
    * the Java version.  To change this, RandomSource would need to be updated
    * to generate numbers using the same algorithm as Java.
    */ 
    if (rand_r(&$actorSymbol(seed)) < $val(trueProbability) * RAND_MAX) {
        $ref(output) = 1;
    } else {
        $ref(output) = 0;	
    } 	
/**/
