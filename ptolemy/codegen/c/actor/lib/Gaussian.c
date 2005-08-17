/*** preinitBlock ***/
    double $actorSymbol(seed);
    int $actorSymbol(haveNextNextGaussian) = 0;
    double $actorSymbol(nextNextGaussian);
    double $actorSymbol(v1), $actorSymbol(v2), $actorSymbol(s);
    double $actorSymbol(multiplier);

    // FIXME: need to support a method for actors to put their private functions
    //int $actorSymbol(next)(int $actorSymbol(bits)) {
    //    $actorSymbol(seed) = (((long long) $actorSymbol(seed) * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
    //    return (int)((signed long long) $actorSymbol(seed) >> (48 - $actorSymbol(bits)));
    //}
     
    //double $actorSymbol(nextDouble)() {
    //    return (((long long)$actorSymbol(next)(26) << 27) + $actorSymbol(next)(27)) / (double)(1LL << 53);
    //}
/**/

/*** setSeedBlock ***/
    //this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    $actorSymbol(seed) = ((long long) $val(seed) ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);
/**/

/*** sharedBlock ***/
    int Gaussian_next(int bits, double* seed) {
        *seed = (((long long) *seed * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) *seed >> (48 - bits));
    }
    
    double Gaussian_nextDouble(double* seed) {
        return (((long long)Gaussian_next(26, seed) << 27) + Gaussian_next(27, seed)) / (double)(1LL << 53);
    }
/**/

/*** fireBlock ***/
    if ($actorSymbol(haveNextNextGaussian)) {
        $actorSymbol(haveNextNextGaussian) = 0;   // false
        $ref(output) = $actorSymbol(nextNextGaussian);
    } else {
        do { 
            $actorSymbol(v1) = 2 * Gaussian_nextDouble(&$actorSymbol(seed)) - 1;   // between -1.0 and 1.0
            $actorSymbol(v2) = 2 * Gaussian_nextDouble(&$actorSymbol(seed)) - 1;   // between -1.0 and 1.0
            $actorSymbol(s) = $actorSymbol(v1) * $actorSymbol(v1) + $actorSymbol(v2) * $actorSymbol(v2);
        } while ($actorSymbol(s) >= 1 || $actorSymbol(s) == 0);

        $actorSymbol(multiplier) = sqrt(-2 * log($actorSymbol(s))/$actorSymbol(s));
        $actorSymbol(nextNextGaussian) = $actorSymbol(v2) * $actorSymbol(multiplier);
        $actorSymbol(haveNextNextGaussian) = 1;   // true
        $ref(output) = $actorSymbol(v1) * $actorSymbol(multiplier);
    }
    $ref(output) = ($ref(output) * $val(standardDeviation)) + $val(mean);    
/**/
