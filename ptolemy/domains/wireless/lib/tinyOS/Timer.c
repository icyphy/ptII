/* Timer.c - test code for the blink app. It is not exactly the same as the TinyOS
Timer component. But it illustrates the similiar structure for our experiments now.


Authors: 
Version $$
*/

#include "Timer.h"
#include "VirtualTinyOS.h"
#include "Blink.h"
#include <stdio.h>

void Timer_StdControl_init() {
    printf("call StdControl.init() of the Timer component. \n");
}

void Timer_StdControl_start() {
    printf("call StdControl.start() of the Timer component. \n");
}

void Timer_StdControl_stop() {
    printf("call StdControl.stop() of the Timer component. \n");
}

void Timer_Timer_start(int period) {
    printf("call the java method to setup timer period. \n");
    setupTimer(period);
}

void Timer_Timer_fired() {
    Blink_Timer_fired();
}

void Timer_Timer_stop() {
  printf("call Timer.stop() of the Timer component. \n");
}