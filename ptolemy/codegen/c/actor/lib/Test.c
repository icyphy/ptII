/***preinitBlock***/
// FIXME: how do we reset count?
int $actorSymbol(count) = 0; 
/**/

/***codeBlock1***/
// Test
// FIXME: Need to handle all channels in the multiport input.
if ($actorSymbol(count) < $size(correctValues) && fabs($ref(input#0) - $ref(correctValues, $actorSymbol(count))) > $ref(tolerance)) {
    // FIXME: what about types other than double?
    printf("Test fails in iteration %d.\n Value was: %f. Should have been: %f\n",
        $actorSymbol(count), (double)$ref(input#0), (double)$ref(correctValues, $actorSymbol(count)));
}
$actorSymbol(count) ++;
/**/
