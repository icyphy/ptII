/***expBlock***/
// There is no need for hasToken() because input is not a multiport, thus this
// actor can only fire if a token is present at the input.
$put(output, exp($get(input)));
/**/


/***logBlock***/
// There is no need for hasToken() because input is not a multiport, thus this
// actor can only fire if a token is present at the input.
$put(output, log($get(input)));
/**/


/***signBlock***/
// There is no need for hasToken() because input is not a multiport, thus this
// actor can only fire if a token is present at the input.
$put(output, ( $get(input) > 0.0 ) ? 1.0 :
( $get(input) < 0.0 ) ? -1.0 : 0);
/**/


/***squareBlock***/
// There is no need for hasToken() because input is not a multiport, thus this
// actor can only fire if a token is present at the input.
$put(output, $get(input) * $get(input));
/**/


/***sqrtBlock***/
// There is no need for hasToken() because input is not a multiport, thus this
// actor can only fire if a token is present at the input.
$put(output, sqrt($get(input)));
/**/

