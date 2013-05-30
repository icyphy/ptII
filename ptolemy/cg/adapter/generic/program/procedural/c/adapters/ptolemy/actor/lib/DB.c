/*** declareSharedBlock ***/
#define _20LOG10SCALE 8.68588963807 //20.0 * (1.0 / Math.log(10.0))
#include <math.h>
/**/

/***preinitBlock***/
double $actorSymbol(dbOutput);
/**/

/***initBlock***/
$actorSymbol(dbOutput) = 0.0;
/**/

/***fireBlock***/
double $actorSymbol(dbTemp) = $get(input);
if ($actorSymbol(dbTemp) <= 0.0) {
    $put(output, $val(min));
} else {
    if ($val(inputIsPower)) {
        $actorSymbol(dbOutput) = log($actorSymbol(dbTemp)) * _20LOG10SCALE / 2.0;
    }  else {
        $actorSymbol(dbOutput) = log($actorSymbol(dbTemp)) * _20LOG10SCALE;
    }
    if ($actorSymbol(dbOutput) < $val(min)) {
        $actorSymbol(dbOutput) = $val(min);
    }
    $put(output, $actorSymbol(dbOutput));
}
/**/

