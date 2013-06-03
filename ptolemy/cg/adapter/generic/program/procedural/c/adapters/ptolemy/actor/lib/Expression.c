/*** sharedBlock ***/
/* max and min may be used by the Expression actor. */
#ifndef max
#define max(a,b) ((a)>(b)?(a):(b))
#endif
#ifndef min
#define min(a,b) ((a)<(b)?(a):(b))
#endif
/**/

/*** preinitBlock ***/
int $actorSymbol(iterationCount);
/**/

/*** initBlock ***/
$actorSymbol(iterationCount) = 1;
/**/

/*** postfireBlock ***/
$actorSymbol(iterationCount)++;
/**/

