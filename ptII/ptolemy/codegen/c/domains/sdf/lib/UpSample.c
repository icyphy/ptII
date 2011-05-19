/*** preinitBlock ***/
        int $actorSymbol(i);
        $targetType(input) $actorSymbol(zero);
/**/

/*** fireBlock($zero) ***/
                $actorSymbol(zero) = $zero;
        for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(factor); $actorSymbol(i)++) {
            if ($actorSymbol(i) == $val(phase)) {
                $ref(output, $actorSymbol(i)) = $ref(input);
            } else {
                $ref(output, $actorSymbol(i)) = $actorSymbol(zero);
            }
        }
/**/

