/***preinitBlock***/
#include "mbed.h"

DigitalOut myled(LED2);
/**/

/*** IntPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        //printf("%d\n", $get(input#$channel));
        Token *token = (*(input->get))((struct IOPort*) input, 0);
        //printf("%d\n", (*(input->get))((struct IOPort*) input, 0)->payload.Boolean);
        printf("%d\n", token->payload.Boolean);
        free(token);
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
// mbed!!
//HACK_MAN: Creating the token and freeing it at the end
//          Should figure out why input->get doesn't free the token
//          Previous code is in comments
//if ($hasToken(input#$channel)) {
if ((*(input->hasToken))((struct IOPort*) input, 0)) {
    Token *token = (*(input->get))((struct IOPort*) input, 0);
    wait(0.05);
    //if ((*(input->get))((struct IOPort*) input, 0)->payload.Boolean) {
    if (token->payload.Boolean) {
        myled = 1;
    } else {
        myled = 0;
    }
    free(token);
}
/**/

/*** TokenPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
}
/**/

