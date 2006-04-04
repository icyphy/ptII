// The algorithm of generating the random number with RandomSource distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** sharedBlock ***/
    int RandomSource_next(int bits, double* seed) {
        *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) *seed >> (48 - bits));
    }
    
    double RandomSource_nextDouble(double* seed) {
        return (((long long)RandomSource_next(26, seed) << 27) + RandomSource_next(27, seed)) / (double)(1LL << 53);
    }
/**/



/*** setSeedBlock0($hashCode) ***/
    $actorSymbol(seed) = $actorSymbol(seed) = time (NULL) + $hashCode;
/**/

/*** setSeedBlock1 ***/
    /* see documentation from http://java.sun.com/j2se/1.4.2/docs/api/java/util/Random.html#setSeed(long) */
    //this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    $actorSymbol(seed) = ((long long) $val(seed) ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);
/**/

/*** preinitBlock ***/
    double $actorSymbol(seed);
/**/
