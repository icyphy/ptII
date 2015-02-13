/***preinitBlock***/
#include "mbed.h"

DigitalOut myled(LED1);
/**/

/*** IntPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%d\n", $get(input#$channel));
}
/**/

/*** DoublePrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%f\n", $get(input#$channel));
}
/**/

/*** StringPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s\n", $get(input#$channel));
}
/**/

/*** BooleanPrintBlock($name, $channel) ***/
// Arduino!
if ($hasToken(input#$channel)) {
    wait(0.1);
    if ($get(input#$channel)) {
        myled = 1;
    } else {
        myled = 0;
    }
}
/**/

/*** TokenPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
}
/**/

