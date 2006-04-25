//#include <deque>
//#include <iostream>
//#include "actor.h"
//#include "port.h"
#include "scheduler.h"
//#include "event.h"
// Jonathan changed from "clock.h", "display.h"
//#include "Clock.h"
//#include "Display.h"

//#include "trigOutIn.h"

#include "TriggeredClock.h"
#include "TriggerOut.h"

int main() {
    //typedef int T;
	Scheduler scheduler;
	
    TriggeredClock tClock (&scheduler);
	TriggerOut triggerOut (&scheduler);
	tClock.output.connect(&(triggerOut.input));
	triggerOut.output.connect(&(tClock.trigger));
	Time endTime = {10, 0};
	Time period = {2, 0};
	Time phase = {1, 0};
	tClock.setEndTime(endTime);
    tClock.setPeriod(period);
    tClock.setPhase(phase);
	tClock.initialize();
	triggerOut.initialize();
	
	std::cout <<"Start execution:"<<std::endl;
	scheduler.execute();
}
