#include "app.h"
#include "ports.h"
#include "gps_isr.h"
#include "ins_isr.h"
#include "filter.h"
#include "statefb.h"
#include "actuator.h"

void app_init() {
  ports_init();
  GPS_ISR_init();
  INS_ISR_init();
  FILTER_init();
  STATEFB_init();
  ACTUATOR_init();
}

void app_start() {
  GPS_ISR_start();
  INS_ISR_start();
  FILTER_start();
  STATEFB_start();
  ACTUATOR_start();
}
