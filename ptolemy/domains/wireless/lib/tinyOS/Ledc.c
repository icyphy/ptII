/* Led.c - test code for the blink app. It is not exactly the same as the TinyOS
Ledc component. But it illustrates the similiar structure for our experiments now.


Authors: 
Version $Id$
*/

#include "Ledc.h"
#include "VirtualTinyOS.h"
#include <stdio.h>

void Ledc_StdControl_init() {
    printf("call StdControl.init() of the Led component. \n");
}

void Ledc_StdControl_start() {
    printf("call StdControl.start() of the Led component. \n");
}

void Ledc_StdControl_stop() {
    printf("call StdControl.stop() of the Led component. \n");
}

void Ledc_Led_blink() {
    printf("Led Blink Blink! \n");
    printf("call the java method to animate blink. \n");
    ledBlink(1);
}
