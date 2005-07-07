/*** preinitBlock ***/
    double $actorSymbol(seed);
    int $actorSymbol(haveNextNextGaussian);
    double $actorSymbol(nextNextGaussian);
    double $actorSymbol(v1), $actorSymbol(v2), $actorSymbol(s);
    double $actorSymbol(multiplier);
/**/

/*** setSeedBlock ***/
    $actorSymbol(seed) = $val(seed);
    $actorSymbol(haveNextNextGaussian) = 0;   // false
/**/

/*** methodBlock ***/
    int $actorSymbol(next)(int $actorSymbol(bits)) {
        $actorSymbol(seed) = ($actorSymbol(seed) * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (int)($actorSymbol(seed) >>> (48 - $actorSymbol(bits)));
    }
     
    double $actorSymbol(nextDouble)() {
        return (((long)$actorSymbol(next)(26) << 27) + $actorSymbol(next)(27)) / (double)(1L << 53);
    }
/**/

/*** fireBlock ***/
    if ($actorSymbol(haveNextNextGaussian)) {
        $actorSymbol(haveNextNextGaussian) = 0;   // false
        $ref(output) = $actorSymbol(nextNextGaussian);
    } else {
        do { 
            $actorSymbol(v1) = 2 * $actorSymbol(nextDouble)() - 1;   // between -1.0 and 1.0
            $actorSymbol(v2) = 2 * $actorSymbol(nextDouble)() - 1;   // between -1.0 and 1.0
            $actorSymbol(s) = $actorSymbol(v1) * $actorSymbol(v1) + $actorSymbol(v2) * $actorSymbol(v2);
        } while ($actorSymbol(s) >= 1 || $actorSymbol(s) == 0);

        $actorSymbol(multiplier) = sqrt(-2 * log($actorSymbol(s))/$actorSymbol(s));
        $actorSymbol(nextNextGaussian) = $actorSymbol(v2) * $actorSymbolmultiplier);
        $actorSymbol(haveNextNextGaussian) = 1;   // true
        $ref(output) = v1 * $actorSymbolmultiplier);
    }
/**/

/*** initBlock ***//**/

/*** wrapupBlock ***//**/

