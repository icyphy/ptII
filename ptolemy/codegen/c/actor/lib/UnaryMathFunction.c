/***expBlock***/
    $ref(output) = exp($ref(input));
/**/


/***logBlock***/
    $ref(output) = log($ref(input));
/**/


/***signBlock***/
    $ref(output) = ( $ref(input) > 0.0 ) ? 1.0 : 
                   ( $ref(input) < 0.0 ) ? -1.0 : 0;
/**/


/***squareBlock***/
    $ref(output) = $ref(input) * $ref(input);
/**/


/***sqrtBlock***/
    $ref(output) = sqrt($ref(input));
/**/