/***expBlock***/
$ref(output) = Math.exp($ref(input));
/**/


/***logBlock***/
$ref(output) = Math.log($ref(input));
/**/


/***signBlock***/
$ref(output) = ( $ref(input) > 0.0 ) ? 1.0 :
( $ref(input) < 0.0 ) ? -1.0 : 0;
/**/


/***squareBlock***/
$ref(output) = $ref(input) * $ref(input);
/**/


/***sqrtBlock***/
$ref(output) = Math.sqrt($ref(input));
/**/

