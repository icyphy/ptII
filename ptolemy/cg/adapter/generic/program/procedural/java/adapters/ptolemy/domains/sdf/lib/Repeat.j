/*** preinitBlock ***/
static int $actorSymbol(i);
static int $actorSymbol(j);
/**/

/***fireBlock***/
{
for ($actorSymbol(i) = 0;
     $actorSymbol(i) < $val(blockSize);
     $actorSymbol(i)++) {
    for ($actorSymbol(j) = 0;
         $actorSymbol(j) < $val(numberOfTimes);
         $actorSymbol(j)++) {
        $put(output, $actorSymbol(j) * $val(blockSize) + $actorSymbol(i), $get(input, $actorSymbol(i)));
    }
}
}
/**/
