/***preinitBlock ($type)***/
int $actorSymbol(iterationCount) = 0;
int $actorSymbol(indexColCount) = 0;
boolean $actorSymbol(match) = false;
$type $actorSymbol(_zero);
/**/

/*** initBlock ($zero) ***/
$actorSymbol(_zero) = $zero;
/**/

/*** fireBlock ***/
if ($actorSymbol(indexColCount) < $size(indexes)
        && $actorSymbol(iterationCount)
                == $ref(indexes, $actorSymbol(indexColCount))) {

    $ref(output) = $ref(values, $actorSymbol(indexColCount));

    $actorSymbol(match) = true;
} else {

    $ref(output) = $actorSymbol(_zero);
}

if ($actorSymbol(iterationCount) <= $ref(indexes, $size(indexes) - 1)) {
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


