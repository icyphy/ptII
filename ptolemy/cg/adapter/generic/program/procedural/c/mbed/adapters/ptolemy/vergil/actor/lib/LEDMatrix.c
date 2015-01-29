/***preinitBlock***/
/* LED Tank Top Code from
 *   http://craftzine.com/01/led
 *   http://www.cs.colorado.edu/~buechley/diy/diy_tank.html
 * However, this code is GPL'd so it is not shipped with Ptolemy II.
 * See ptII/vendors/misc/ledTankTop/LEDMatrix.c
 */

#ifdef __AVR__

#endif /* __AVR__ */

/**/

/***initBlock***/

// Initialize tank pins as output pins.

/**/

/***fireBlock***/

#ifndef __AVR__
/* Machines that don't have the hardware just print 0 and 1. */
if ($ref(row) == 0) {
    printf("\n");
}
if ($ref(row) == 0 && $ref(column) == 0) {
    printf("\n");
}
if ($ref(control)) {
    printf("1");
} else {
    printf("0");
}

#else /* !  __AVR__ */


#endif /* ! __AVR__ */

/**/
