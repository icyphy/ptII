/*** sharedBlock ***/
#if PT_NO_RINT
/* Atmel AVR does not have rint() */
#define rint(x) (floor((x)+0.5f))
#endif
/**/

/*** ceilBlock ***/
$ref(output) = ceil($ref(input));
/**/

/*** floorBlock ***/
$ref(output) = floor($ref(input));
/**/

/*** roundBlock ***/
$ref(output) = rint($ref(input));
/**/

/*** truncateBlock ***/
$ref(output) = trunc($ref(input));
/**/
