/***initBlock*/
// FIXME: how do we reset count?
int count = 0; 
/**/

/***codeBlock1*/
// Test
if (abs($ref(input) - $ref(correctValues, count)) > $ref(tolerance)) {
    // FIXME: what about types other than double?
    printf("Test fails in iteration %d.\n Value was: %f. Should have been: %f\n",
        count, (double)$ref(input), (double)$ref(correctValues, count));
}
count++;
/**/