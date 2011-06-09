/***preinitBlock($type) ***/
int $actorSymbol(iterationCount) = 0;
int $actorSymbol(indexColCount) = 0;
boolean $actorSymbol(match) = false;
$type $actorSymbol(_zero);

int $actorSymbol(LimitedFiringSource_iterationCount) = 0;
/**/

/*** initBlock($zero) ***/
$actorSymbol(_zero) = $zero;
/**/

/*** fireBlock ***/
if ($actorSymbol(indexColCount) < $size(indexes)
        && $actorSymbol(iterationCount)
                == $param(indexes, $actorSymbol(indexColCount))) {

    $put(output, $param(values, $actorSymbol(indexColCount)));

    $actorSymbol(match) = true;
} else {

    $put(output, $actorSymbol(_zero));
}

if ($actorSymbol(iterationCount) <= $param(indexes, $size(indexes) - 1)) {
    $actorSymbol(iterationCount) ++;
}

if ($actorSymbol(match)) {
    $actorSymbol(indexColCount) ++;
    $actorSymbol(match) = false;
}

if ($actorSymbol(indexColCount) >= $size(indexes) && $val(repeat)) {
    $actorSymbol(iterationCount) = 0;
    $actorSymbol(indexColCount) = 0;
}
/**/

/*** postfireBlock() ***/
$actorSymbol(LimitedFiringSource_iterationCount)++;
if ($val(firingCountLimit) == $actorSymbol(LimitedFiringSource_iterationCount)) {
   // Return from run()
   return false;
}
/**/
