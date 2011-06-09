/***preinitBlock***/
double $actorSymbol(sum) = 0;
int $actorSymbol(count) = 0;
/**/

/***outputBlock***/
$actorSymbol(count)++;
$actorSymbol(sum) += $ref(input);
$ref(output) = $actorSymbol(sum) / $actorSymbol(count);
/**/

/***resetBlock***/
if ($ref(reset)) {
    $actorSymbol(sum) = 0;
    $actorSymbol(count) = 0;
}
/**/
