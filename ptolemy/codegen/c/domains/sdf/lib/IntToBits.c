/*** preinitBlock ***/
        int $actorSymbol(i);
        int $actorSymbol(integer);
/**/

/*** fireBlock ***/
                $actorSymbol(integer) = $ref(input);
        if ($actorSymbol(integer) < 0) {
            $ref(output, 0) = true;

            //$actorSymbol(integer) = (int)(2147483648LL + $actorSymbol(integer));
            $actorSymbol(integer) = ((1 << ($val(numberOfBits) - 1)) + $actorSymbol(integer));
        } else {
            $ref(output, 0) = false;
        }

        for ($actorSymbol(i) = $val(numberOfBits) - 1; $actorSymbol(i) > 0; $actorSymbol(i)--) {
            $ref(output, $actorSymbol(i)) = $actorSymbol(integer) % 2;
            $actorSymbol(integer) /= 2;
        }
/**/

