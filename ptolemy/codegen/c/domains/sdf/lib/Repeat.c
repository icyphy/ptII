/*** preinitBlock ***/
int $actorSymbol(i);
int $actorSymbol(j);
/**/

/***fireBlock***/

for ($actorSymbol(i) = 0; 
     $actorSymbol(i) < $ref(blockSize);
     $actorSymbol(i)++) {
    for ($actorSymbol(j) = 0; 
         $actorSymbol(j) < $ref(numberOfTimes);
         $actorSymbol(j)++) {
        $ref(output, $actorSymbol(j) * $ref(blockSize) + $actorSymbol(i)) =
            $ref(input, $actorSymbol(i));
    }
}
/**/
