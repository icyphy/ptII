#ifndef __ISR_H__
#define __ISR_H__

#define INTERRUPT_PERIOD_uSEC 1000000

void ISR_init(void);

void ISR_start(void);

void* runGPS();

#endif
