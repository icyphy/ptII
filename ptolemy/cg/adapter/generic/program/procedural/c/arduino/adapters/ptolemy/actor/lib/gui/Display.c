/***preinitBlock***/
#ifdef __AVR__
#include <Arduino.h>
int led = 13;
void setup() {
    pinMode(led,OUTPUT);
}
#endif
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
#ifdef __AVR__
    if ($get(input#$channel)) {
        digitalWrite(led, HIGH);
    } else {
        digitalWrite(led, LOW);
    }
#else
        printf("%d Arduino!!!\n", $get(input#$channel));
#endif __AVR__
}
/**/

/*** TokenPrintBlock($name, $channel) ***/
if ($hasToken(input#$channel)) {
        printf("%s: ", $tokenFunc($get(input#$channel)::toString()).payload);
}
/**/

