/* Blink.c - test code for the blink app. It is not exactly the same as the TinyOS
Blink component. But it illustrates the similiar structure for our experiments now.


Authors: Yang
Version $$
*/

#include "Blink.h"
#include "Ledc.h"
#include <stdio.h>

void Blink_StdControl_init() {
    Ledc_StdControl_init();
}

void Blink_StdControl_start() {
    Ledc_StdControl_start();
    int period = 1;
    Blink_Timer_start(period);
}

void Blink_StdControl_stop() {
    Ledc_StdControl_stop();
    Blink_Timer_stop();
}

void Blink_Timer_start(int period) {
    printf("call Blink.start() of the Timer component. \n");
    Timer_Timer_start(period);
}

void Blink_Timer_fired() {
    Ledc_Led_blink(); 
}

void Blink_Timer_stop() {
  Timer_Timer_stop();
}




