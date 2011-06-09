/***fireBlock***/
$put(output, ( $get(input) < $val(bottom) )? $val(bottom) : ( $get(input) > $val(top) )? $val(top) : $get(input));
/**/
