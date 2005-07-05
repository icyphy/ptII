/***preinitBlock***/
double $actorSymbol(sum) = 0;
int $actorSymbol(count) = 0; 
/**/

/***codeBlock1***/
if ($ref(reset)) {
    $actorSymbol(sum) = 0;
    $actorSymbol(count) = 0;
} else {
    $actorSymbol(sum) += $ref(input);
    $actorSymbol(count)++;
    $ref(output) = $actorSymbol(sum) / $actorSymbol(count);
}
/**/
