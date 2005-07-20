/*** preinitBlock ***/
    int $actorSymbol(i);
/**/

//<type> in fireBlock is the type of the output array.
//<size> is the number of input ports.
/*** fireBlock(<type>, <size>)***/
    $ref(output) = malloc(size* sizeof(<type>));

    for ($actorSymbol(i) = 0; $actorSymbol(i) < <size>; $actorSymbol(i)++) {
        $ref(output)[$actorSymbol(i)] = $ref(input, $actorSymbol(i));
    }
/**/