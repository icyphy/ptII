/***preinitBlock***/
int $actorSymbol(currentIndex);
boolean $actorSymbol(outputProduced);
/**/

/***initBlock***/
$actorSymbol(currentIndex) = 0;
$actorSymbol(outputProduced) = false;
/**/

/***codeBlock1***/
if ($actorSymbol(currentIndex) < $size(values)) {
    $put(output, $param(values, $actorSymbol(currentIndex)));
    $actorSymbol(outputProduced) = true;
};
/**/


/***codeBlock2***/
if ($get(enable) && $actorSymbol(currentIndex) < $size(values)) {
    $put(output, $param(values, $actorSymbol(currentIndex)));
    $actorSymbol(outputProduced) = true;
}
/**/


/***codeBlock3***/
if ($actorSymbol(outputProduced)) {
    $actorSymbol(outputProduced) = false;
    $actorSymbol(currentIndex) += 1;
    if ($actorSymbol(currentIndex) >= $size(values)) {
        if ($val(repeat)) {
            // Code for the case where repeat is true
            $actorSymbol(currentIndex) = 0;
        } else {
            // Code for the case where repeat is false
            //To prevent overflow...
            $actorSymbol(currentIndex) = $size(values);
        }
    }
}
/**/

