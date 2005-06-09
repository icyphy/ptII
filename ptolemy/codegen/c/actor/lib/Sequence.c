/***preinitBlock*/
int $actorSymbol(currentIndex) = 0;
int $actorSymbol(outputProduced) = 0;
/**/



/***codeBlock1*/
if ($actorSymbol(currentIndex) < $size(values)) {
    $ref(output) = $ref(values, $actorSymbol(currentIndex));
    $actorSymbol(outputProduced) = 1;
};
/**/


/***codeBlock2*/
if ($ref(enable) != 0 && $actorSymbol(currentIndex) < $size(values)) {
    $ref(output) = $ref(values, $actorSymbol(currentIndex));
    $actorSymbol(outputProduced) = 1;
}
/**/



/***codeBlock3*/
if ($actorSymbol(outputProduced) != 0) {
    $actorSymbol(outputProduced) = 0;
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
