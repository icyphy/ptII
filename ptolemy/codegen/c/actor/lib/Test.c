/***initBlock*/
// FIXME: how do we reset count?
int count = 0; 
/**/

/***codeBlock1*/
// Test
// FIXME: Need to handle all channels in the multiport input.
if (count < $size(correctValues) && abs($ref(input#0) - $ref(correctValues, count)) > $ref(tolerance)) {
    // FIXME: what about types other than double?
    printf("Test fails in iteration %d.\n Value was: %f. Should have been: %f\n",
        count, (double)$ref(input#0), (double)$ref(correctValues, count));
}
count++;
/**/