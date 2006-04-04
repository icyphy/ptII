// The algorithm of generating the Bernoulli distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/***randomBlock***/
    if (RandomSource_nextDouble(&$actorSymbol(seed)) < $val(trueProbability)) {
        $ref(output) = true;
    } else {
        $ref(output) = false;	
    } 	
/**/

