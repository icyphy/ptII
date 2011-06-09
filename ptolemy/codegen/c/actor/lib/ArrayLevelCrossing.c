/*** preinitBlock ***/
int $actorSymbol(inputSize);
int $actorSymbol(increment);
double $actorSymbol(reference);
double $actorSymbol(thresholdValue);
int $actorSymbol(i);
double $actorSymbol(currentValue);
/**/

/*** fireBlock ***/
$actorSymbol(inputSize) = $ref(array).payload.DoubleArray->size;
if (($ref(start) >= $actorSymbol(inputSize)) || ($ref(start) < 0)) {
    // error;
    fprintf(stderr, "start is out of range: %d", $ref(start));
    exit(1);
}
$actorSymbol(reference) = DoubleArray_get($ref(array), $ref(start));
$actorSymbol(thresholdValue) = $val(threshold);
$actorSymbol(increment) = -1;
/**/

/***forwardBlock***/
$actorSymbol(increment) = 1;
/**/



/***amplitude_above***/
$actorSymbol(thresholdValue) = $actorSymbol(reference) * pow(10.0, ($actorSymbol(thresholdValue) / 20));
/**/

/***amplitude_notAbove***/
$actorSymbol(thresholdValue) = $actorSymbol(reference) * pow(10.0, (-$actorSymbol(thresholdValue) / 20));
/**/

/***power_above***/
$actorSymbol(thresholdValue) = $actorSymbol(reference) * pow(10.0, ($actorSymbol(thresholdValue) / 10));
/**/

/***power_notAbove***/
$actorSymbol(thresholdValue) = $actorSymbol(reference) * pow(10.0, (-$actorSymbol(thresholdValue) / 10));
/**/

/***linear_above***/
$actorSymbol(thresholdValue) += $actorSymbol(reference);
/**/

/***linear_notAbove***/
$actorSymbol(thresholdValue) = $actorSymbol(reference) - $actorSymbol(thresholdValue);
/**/




/***findCrossing_above***/
// Default output if we don't find a crossing.
$ref(output) = -1;
for ($actorSymbol(i) = $ref(start); ($actorSymbol(i) < $actorSymbol(inputSize)) && ($actorSymbol(i) >= 0); $actorSymbol(i) += $actorSymbol(increment)) {
    $actorSymbol(currentValue) = DoubleArray_get($ref(array), $actorSymbol(i));

    // Searching for values above the threshold.
    if ($actorSymbol(currentValue) > $actorSymbol(thresholdValue)) {
        $ref(output) = $actorSymbol(i);
        break;
    }
}
/**/

/***findCrossing_notAbove***/
// Default output if we don't find a crossing.
$ref(output) = -1;
for ($actorSymbol(i) = $ref(start); ($actorSymbol(i) < $actorSymbol(inputSize)) && ($actorSymbol(i) >= 0); $actorSymbol(i) += $actorSymbol(increment)) {
    $actorSymbol(currentValue) = DoubleArray_get($ref(array), $actorSymbol(i));

    // Searching for values below the threshold.
    if ($actorSymbol(currentValue) < $actorSymbol(thresholdValue)) {
        $ref(output) = $actorSymbol(i);
        break;
    }
}
/**/


