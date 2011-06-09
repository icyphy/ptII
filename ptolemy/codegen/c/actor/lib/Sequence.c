/***preinitBlock***/
int $actorSymbol(currentIndex) = 0;
boolean $actorSymbol(outputProduced) = false;
/**/

/***ignoreEnable***/
if ($actorSymbol(currentIndex) < $size(values)) {
    $ref(output) = $ref(values, $actorSymbol(currentIndex));
    $actorSymbol(outputProduced) = true;
    $send(output, 0)
};
$this.postfireBlock()
/**/


/***checkEnable***/
$get(enable, 0)

if ($ref(enable) && $actorSymbol(currentIndex) < $size(values)) {
    $ref(output) = $ref(values, $actorSymbol(currentIndex));
    $actorSymbol(outputProduced) = true;
    $send(output, 0)
}
$this.postfireBlock()
/**/


/***postfireBlock***/
if ($actorSymbol(outputProduced)) {
    $actorSymbol(outputProduced) = false;
    $actorSymbol(currentIndex) += 1;
    if ($actorSymbol(currentIndex) >= $size(values)) {
        #if $val(repeat)
            // Code for the case where repeat is true
            $actorSymbol(currentIndex) = 0;
        #else
            // Code for the case where repeat is false
            //To prevent overflow...
            $actorSymbol(currentIndex) = $size(values);
        #endif
    }
}
/**/

