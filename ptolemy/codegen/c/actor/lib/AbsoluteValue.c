/***fireBlock***/
    // Cannot use abs() 
    //$ref(output) = abs($ref(input));
    $ref(output) = $ref(input) < 0.0 ? -$ref(input) : $ref(input);
/**/
