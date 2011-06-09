/*** initBlock ***/
    $ref(output) = $new(Complex(0, 0));
/**/


/*** fireBlock ***/
    $ref(output).payload.Complex->real = $ref(x);
    $ref(output).payload.Complex->imag = $ref(y);
/**/
