/*** preinitBlock ***/
    double $actorSymbol(seed);
/**/

/*** setSeedBlock ***/
    $actorSymbol(seed) = $val(seed);
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
    $ref(output) = ($actorSymbol(nextDouble)() * ($val(upperValue) - $val(lowerValue))) + $val(lowerValue);
/**/

