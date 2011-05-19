/***preinitBlock($type)***/
$type $actorSymbol(i);
$targetType(input) $actorSymbol(zero);
/**/

/*** fireBlock($zero) ***/
$actorSymbol(zero) = $zero;
for ($actorSymbol(i) = 0; $actorSymbol(i) < $val(factor); $actorSymbol(i)++) {
    if ($actorSymbol(i) == $val(phase)) {
        $put(output, $actorSymbol(i), $get(input));
    } else {
        $put(output, $actorSymbol(i), $actorSymbol(zero));
    }
}
/**/

