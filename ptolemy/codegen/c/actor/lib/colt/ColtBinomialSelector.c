/***preinitBlock***/
long *$actorSymbol(_current);
// From RandomSource.c
double $actorSymbol(seed);
/**/

/***binomialDistributionBlock ***/
$ref(output) = RandomSource_BinomialDistribution(1, 0.5, &$actorSymbol(seed));
/**/

/*** fireBlock0($populationWidth)***/
long sourceValues[$populationWidth];
long sourceTotal = 0;
int i;
    

/*** fireBlock1($channel)***/
sourceValues[i] = $ref(populations, $channel);
if (sourceValues[i] < 0) {
    fprintf(stderr, "$ref(populations) was < 0\n");
    exit(-1);
 }
sourceTotal += sourceValues[i];

/*** fireBlock2($populationWidth)***/
// Process the binomial selections.

int trialsRemaining = $ref(trials);
long sourcePool = sourceTotal;
if ($actorSymbol(_current) != 0) {
    free(_current);
}
$actorSymbol(_current) = malloc($populationWidth * sizeof(long));
for (i=0; i < $populationWidth; i++) {
    int selected = 0;
    if ((trialsRemaining > 0) && (sourceValues[i] > 0)) {
        double p = (double) sourceValues[i] / (double) sourcePool;
        if (p < 1.0) {
            selected = RandomSource_BinomialDistribution(trialsRemaining, p, &$actorSymbol(seed));
        } else {
            selected = trialsRemaining;
        }
    }
    $actorSymbol(_current)[i] = selected;
    trialsRemaining -= selected;
    sourcePool -= $actorSymbol(sourceValue)[i];
}
/**/

/***fireBlock($channel)***/
$ref(output, $channel) = $actorSymbol(_current)[i]
/**/
