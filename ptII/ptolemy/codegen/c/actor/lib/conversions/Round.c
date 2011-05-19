/*** sharedBlock ***/
#if PT_NO_ROUND
/* Atmel AVR might not have round() */
#define round(x) (floor((x)+0.5f))
#endif
/**/

/*** ceilBlock ***/
$ref(output) = ceil($ref(input));
/**/

/*** floorBlock ***/
$ref(output) = floor($ref(input));
/**/

/*** roundBlock ***/
$ref(output) = round($ref(input));
/**/

/*** truncateBlock ***/
$ref(output) = trunc($ref(input));
/**/
