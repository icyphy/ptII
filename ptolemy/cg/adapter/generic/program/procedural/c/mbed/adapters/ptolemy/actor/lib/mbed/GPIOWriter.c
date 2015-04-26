/***preinitBlock***/
#include "mbed.h"

//DigitalOut myled(LED2);
DigitalOut * $actorSymbol(myled);
/**/

//***initBlock***/
switch($param(BoardPortNumber))
{
  case 0:
    $actorSymbol(myled) = new DigitalOut(LED1);
    break;
  case 2:
    $actorSymbol(myled) = new DigitalOut(LED3);
    break;
  case 3:
    $actorSymbol(myled) = new DigitalOut(LED4);
    break;
  case 1:
  default:
    $actorSymbol(myled) = new DigitalOut(LED2);
    break;
}
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
        *$actorSymbol(myled) = 1;
    } else {
        *$actorSymbol(myled) = 0;
    }
    free(token);
}
/**/

/*** TokenPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
}
/**/

