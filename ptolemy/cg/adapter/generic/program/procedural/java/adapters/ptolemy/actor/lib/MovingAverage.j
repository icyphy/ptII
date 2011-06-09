/*** CommonPreinitBlock($type) ***/
int $actorSymbol(count);
double $actorSymbol(factor);
int $actorSymbol(mostRecent);
$type $actorSymbol(sum);
$type [] $actorSymbol(data) = new $type[$val(maxPastInputsToAverage)];
//int $actorSymbol(typeSize) = sizeof($type);
/**/

/*** initBlock ***/
$actorSymbol(count) = 0;
$actorSymbol(mostRecent) = $val(maxPastInputsToAverage);
//$actorSymbol(data) = malloc($val(maxPastInputsToAverage) * $actorSymbol(typeSize));
/**/

/***fireBlock***/
{
    int $actorSymbol(i);
    if ($actorSymbol(mostRecent) <= 0) {
        $actorSymbol(mostRecent) = $val(maxPastInputsToAverage) - 1;
    } else {
        --$actorSymbol(mostRecent);
    }
    $actorSymbol(data)[$actorSymbol(mostRecent)] = $get(input);
    if ($actorSymbol(count) < $val(maxPastInputsToAverage)) {
        $actorSymbol(count)++;
        $actorSymbol(factor) = 1.0/$actorSymbol(count);
    }
    $actorSymbol(sum) = $actorSymbol(data)[$actorSymbol(mostRecent)];
    for ($actorSymbol(i) = 1; $actorSymbol(i) < $actorSymbol(count); $actorSymbol(i)++) {
        int dataIndex = ($actorSymbol(mostRecent) + $actorSymbol(i) ) % ($val(maxPastInputsToAverage));
        $actorSymbol(sum) += $actorSymbol(data)[dataIndex];
    }
    $put(output, $actorSymbol(factor) * $actorSymbol(sum));
}
/**/
