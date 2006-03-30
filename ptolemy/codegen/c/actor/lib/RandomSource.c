// The algorithm of generating the random number with Gaussian distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.

/*** sharedBlock ***/
    int $actorClass(next)(int bits, double* seed) {
        *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) *seed >> (48 - bits));
    }
    
    double $actorClass(nextDouble)(double* seed) {
        return (((long long)Gaussian_next(26, seed) << 27) + Gaussian_next(27, seed)) / (double)(1LL << 53);
    }
/**/

/***preinitBlock***/

/**/
