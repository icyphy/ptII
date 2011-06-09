/*** preinitBlock ***/
int $actorSymbol(trueCount);
int $actorSymbol(i);
/**/

/*** fireBlock ***/
$actorSymbol(trueCount) = 0;

for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(blockSize); $actorSymbol(i)++) {
    if ($ref(input, $actorSymbol(i))) {
        $actorSymbol(trueCount)++;
    }
}
$ref(output) = $actorSymbol(trueCount);
/**/
