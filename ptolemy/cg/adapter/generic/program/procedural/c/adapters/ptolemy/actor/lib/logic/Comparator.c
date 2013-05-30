/*** GTBlock ***/
$put(output, ($get(left) + $val(tolerance)) > $get(right));
/**/

/*** GEBlock ***/
$put(output, ($get(left) + $val(tolerance)) >= $get(right));
/**/

/*** LTBlock ***/
$put(output, $get(left) < ($get(right) + $val(tolerance)));
/**/

/*** LEBlock ***/
$put(output, $get(left) <= ($get(right) + $val(tolerance)));
/**/

/*** EQBlock ***/
$lcCgType(left) tempLeft = $get(left);
$lcCgType(right) tempRight = $get(right);
$put(output, (tempLeft <= (tempRight + $val(tolerance)))
        && (tempLeft >= (tempRight - $val(tolerance))));
/**/

