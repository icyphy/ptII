#include "app.h"
#include "ports.h"
#include "actor1.h"
#include "actor2.h"
#include "isr.h"

void app_init() {
  ports_init();
  ACTOR1_init();
  ACTOR2_init();
  ISR_init();
}

void app_start() {
  ACTOR1_start();
  ACTOR2_start();
  ISR_start();
}
