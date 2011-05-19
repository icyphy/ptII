/*** preinitBlock ***/
Token $actorSymbol(levels);
double* $actorSymbol(transitionPoints);
int $actorSymbol(i);
/**/

//$numPoints is number of transition points (# of levels - 1)

/*** initBlock ($numPoints)***/
$actorSymbol(levels) = $val(levels);
// allocate space to store levels and transitionPoints
// We use CALLOC to optimizate array performance
$actorSymbol(transitionPoints) =
(double*) calloc($numPoints, sizeof(double));

for ($actorSymbol(i) = 0;
     $actorSymbol(i) < ($numPoints - 1);
     $actorSymbol(i)++) {
    // transitionPoint[I] = (levels[I] + levels[I+1]) / 2;
    $actorSymbol(transitionPoints)[$actorSymbol(i)] =
        ($actorSymbol(levels).payload.DoubleArray->elements[$actorSymbol(i)]
                + $actorSymbol(levels).payload.DoubleArray->elements[$actorSymbol(i) + 1]) / 2.0;
}
/**/

/*** fireBlock ($numPoints)***/
for ($actorSymbol(i) = 0;
     $actorSymbol(i) < $numPoints;
     $actorSymbol(i)++) {
    if ($ref(input)
            <= $actorSymbol(transitionPoints)[$actorSymbol(i)]) {
        $ref(output) = $actorSymbol(levels).payload.DoubleArray->elements[$actorSymbol(i)];
        break;
    }
}
if ($actorSymbol(i) >= $numPoints) {
    // There was no match in the for loop above.
    $ref(output) = $actorSymbol(levels).payload.DoubleArray->elements[$numPoints-1];

}

/**/

/*** wrapupBlock ***/
free($actorSymbol(transitionPoints));
/**/

