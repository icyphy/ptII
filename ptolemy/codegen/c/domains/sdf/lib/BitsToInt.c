/*** preinitBlock ***/
        int $actorSymbol(i);
        int $actorSymbol(value);
/**/

/*** fireBlock ***/
        $actorSymbol(value) = 0;

        for ($actorSymbol(i) = 1; $actorSymbol(i) < $val(numberOfBits); $actorSymbol(i)++) {
            $actorSymbol(value) = ($actorSymbol(value) << 1) + $ref(input, $actorSymbol(i));
        }

        if ($ref(input, 0)) {
            //convert integer to negative value.
            $actorSymbol(value) = $actorSymbol(value) - (1 << ($val(numberOfBits) - 1));
        }

        $ref(output) = $actorSymbol(value);
/**/

