/*** sharedBlock ***/
static final double _20LOG10SCALE = 20.0 * (1.0 / Math.log(10.0));
/**/

/***preinitBlock***/
double $actorSymbol(dbOutput) = 0.0;
/**/

/***fireBlock***/
if ($get(input) <= 0.0) {
    $put(output, $val(min));
} else {
    if ($val(inputIsPower)) {
        $actorSymbol(dbOutput) = Math.log($get(input)) * _20LOG10SCALE / 2.0;
    }  else {
        $actorSymbol(dbOutput) = Math.log($get(input)) * _20LOG10SCALE;
    }
    if ($actorSymbol(dbOutput) < $val(min)) {
        $actorSymbol(dbOutput) = $val(min);
    }
    $put(output, $actorSymbol(dbOutput));
}
/**/

