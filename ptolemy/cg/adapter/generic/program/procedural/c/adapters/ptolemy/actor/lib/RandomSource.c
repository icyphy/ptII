// The algorithm of generating the random number with RandomSource distribution
// is based on source code from Java.util.Random. Given the same seed, it
// generates the same list of random numbers as the java.util.Random object.


/*** sharedBlock ***/
#ifdef PT_NO_TIME
/* Atmel AVR does not have time() */
#define time(x)
#endif
int RandomSource_next(int bits, double* seed) {
    *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
    return (int)((signed long long) *seed >> (48 - bits));
}

double RandomSource_nextDouble(double* seed) {
    return (((long long)RandomSource_next(26, seed) << 27) + RandomSource_next(27, seed)) / (double)(1LL << 53);
}
/**/



/*** gaussianBlock ***/

double RandomSource_nextGaussian(double* seed, boolean* haveNextNextGaussian, double* nextNextGaussian) {
    double multiplier;
    double v1;
    double v2;
    double s;

    if (*haveNextNextGaussian) {
        *haveNextNextGaussian = false;
        return *nextNextGaussian;
    } else {
        do {
            v1 = 2 * RandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
            v2 = 2 * RandomSource_nextDouble(seed) - 1;   // between -1.0 and 1.0
            s = v1 * v1 + v2 * v2;
        } while (s >= 1 || s == 0);

        multiplier = sqrt(-2 * log(s)/s);
        *nextNextGaussian = v2 * multiplier;
        *haveNextNextGaussian = true;
        return v1 * multiplier;
    }
}

/**/

/*** setSeedBlock0($hashCode) ***/
$actorSymbol(seed) = time (NULL) + $hashCode;
/**/

/*** setSeedBlock1($hashCode) ***/
/* see documentation from http://java.sun.com/j2se/1.4.2/docs/api/java/util/Random.html#setSeed(long) */
//this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
$actorSymbol(seed) = (((long long) $val(seed) + $hashCode) ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);
/**/

/*** preinitBlock ***/
double $actorSymbol(seed);
/**/


