/*** sharedBlock ***/
        $super();
        int $actorClass(j);
/**/

/*** initBlock ***/
        $actorSymbol(_taps) = $typeFunc(TYPE_Array::clone($ref(taps)));
        $super();
/**/

/*** fireBlock ***/
        $actorSymbol(_taps) = $ref(newTaps);
    $super.prefireBlock();

    $super.fireBlock0();
    for ($actorClass(j) = 0; $actorClass(j) < $val(blockSize); $actorClass(j)++) {
        $super();
    }
/**/

