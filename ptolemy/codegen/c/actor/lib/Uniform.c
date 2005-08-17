/*** preinitBlock ***/
    double $actorSymbol(seed);
    int $actorSymbol(haveNextNextGaussian) = 0;
    double $actorSymbol(nextNextGaussian);
    double $actorSymbol(v1), $actorSymbol(v2), $actorSymbol(s);
    double $actorSymbol(multiplier);

    // FIXME: need to support a method for actors to put their private functions
    int $actorSymbol(next)(int $actorSymbol(bits)) {
        $actorSymbol(seed) = (((long long) $actorSymbol(seed) * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) $actorSymbol(seed) >> (48 - $actorSymbol(bits)));
    }
     
    double $actorSymbol(nextDouble)() {
        return (((long long)$actorSymbol(next)(26) << 27) + $actorSymbol(next)(27)) / (double)(1LL << 53);
    }
/**/

/*** setSeedBlock ***/
    //this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    $actorSymbol(seed) = ((long long) $val(seed) ^ 0x5DEECE66DLL)  & ((1LL << 48) - 1);
/**/

/*** methodBlock ***/
    int $actorSymbol(next)(int $actorSymbol(bits)) {
        $actorSymbol(seed) = (((long long) $actorSymbol(seed) * 0x5DEECE66DLL) + 0xBLL) & ((1LL << 48) - 1);
        return (int)((signed long long) $actorSymbol(seed) >> (48 - $actorSymbol(bits)));
    }
     
    double $actorSymbol(nextDouble)() {
        return (((long long)$actorSymbol(next)(26) << 27) + $actorSymbol(next)(27)) / (double)(1LL << 53);
    }
/**/

/*** fireBlock ***/
    $ref(output) = ($actorSymbol(nextDouble)() * ($val(upperValue) - $val(lowerValue))) + $val(lowerValue);
/**/

