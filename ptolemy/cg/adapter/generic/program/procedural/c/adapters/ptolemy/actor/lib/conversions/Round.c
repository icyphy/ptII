/*** sharedBlock ***/
#if PT_NO_ROUND
/* Atmel AVR might not have round() */
#define round(x) (floor((x)+0.5f))
#endif
/**/

/*** ceilBlock() ***/
$put(output, ceil($get(input)));
/**/

/*** floorBlock() ***/
$put(output, (double)(floor(($get(input)))));
/**/

/*** roundBlock() ***/
$put(output, round($get(input)));
/**/

/*** truncateBlock() ***/
$put(output, trunc($get(input)));
/**/
