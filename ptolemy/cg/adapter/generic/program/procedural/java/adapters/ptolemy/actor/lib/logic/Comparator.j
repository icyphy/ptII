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
$put(output, ($get(left) <= ($get(right) + $val(tolerance))) && ($get(left) >= ($get(right) - $val(tolerance))));
/**/

