/*** GTBlock ***/
$ref(output) = ($ref(left) + $val(tolerance)) > $ref(right);
/**/

/*** GEBlock ***/
$ref(output) = ($ref(left) + $val(tolerance)) >= $ref(right);
/**/

/*** LTBlock ***/
$ref(output) = $ref(left) < ($ref(right) + $val(tolerance));
/**/

/*** LEBlock ***/
$ref(output) = $ref(left) <= ($ref(right) + $val(tolerance));
/**/

/*** EQBlock ***/
$ref(output) = ($ref(left) <= ($ref(right) + $val(tolerance)))
        && ($ref(left) >= ($ref(right) - $val(tolerance)));
/**/

