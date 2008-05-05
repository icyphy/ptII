/** If the input is true, then
 *  busy wait for the number of milliseconds
 *  given by the milliseconds parameter.
 */

/***preinitBlock***/
/**/

/***initBlock***/
/**/

/***fireBlock***/
    if($ref(trigger)) {
      delay1ms($val(milliseconds));
   }
   $ref(done) = true;
/**/

/***wrapupBlock***/
/**/

