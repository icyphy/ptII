/*** preinitializeFiringCountLimitBlock() ***/
int $actorSymbol(LimitedFiringSource_iterationCount) = 0;
/**/

/*** postfireFiringCountLimitBlock() ***/
$actorSymbol(LimitedFiringSource_iterationCount)++;
if ($val(firingCountLimit) == $actorSymbol(LimitedFiringSource_iterationCount)) {
   // Return from run()
   return false;
}
/**/


