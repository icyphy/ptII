/***preinitBlock***/
    int $actorSymbol(currentIndex) = 0;
    boolean $actorSymbol(outputProduced) = false;
/**/

/***codeBlock1***/
    if ($actorSymbol(currentIndex) < $size(values)) {
        $ref(output) = $ref(values, $actorSymbol(currentIndex));
        $actorSymbol(outputProduced) = true;
    };
/**/


/***codeBlock2***/
    if ($ref(enable) != 0 && $actorSymbol(currentIndex) < $size(values)) {
        $ref(output) = $ref(values, $actorSymbol(currentIndex));
        $actorSymbol(outputProduced) = true;
    }
/**/


/***codeBlock3***/
    if ($actorSymbol(outputProduced)) {
        $actorSymbol(outputProduced) = false;
        $actorSymbol(currentIndex) += 1;
        if ($actorSymbol(currentIndex) >= $size(values)) {
            if ($val(repeat)) {
               $actorSymbol(currentIndex) = 0;
            } else {
               //To prevent overflow...
               $actorSymbol(currentIndex) = $size(values);
            }
        }
    }
/**/

