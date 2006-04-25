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
	
	/*Clock clock (&scheduler);
	DEToHDFCompositeActor scale (&scheduler);
	Display display(&scheduler);
	clock.output.connect(&(scale.input));
	scale.output.connect(&(display.input));
	Time endTime = {1000, 0};
	clock.setEndTime(endTime);

	//DependencyLink* link = new DependencyLink(&(clock.fireAt), 300.0);
	//display.input.setDependencyLink(link);
	clock.initialize();
	scale.initialize();
	display.initialize();

	std::cout <<"Start execution:"<<std::endl;
	scheduler.execute();
    //(*e2).printContent();	*/

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
	//DependencyLink* link = new DependencyLink(&(clock.fireAt), 300.0);
	//display.input.setDependencyLink(link);
	tClock.initialize();
	triggerOut.initialize();
	
	std::cout <<"Start execution:"<<std::endl;
	scheduler.execute();
    //(*e2).printContent();	
}

/*
int ymain() {
    typedef int T;
	Scheduler scheduler;
	Clock clock (&scheduler);
	Display display(&scheduler);
	clock.output.connect(&(display.input));
	Time endTime = {1000, 0};
	clock.setEndTime(endTime);

	clock.initialize();
	display.initialize();

//	/*Token<T> token (1);
	Time time = {100, 10};
	Event<T> e (token, time);
	Event<T>* e2;
	Scheduler scheduler;

	Port<T> port (false);
	scheduler.registePort(&port);
	scheduler.postEvent(token, &port, time);
	scheduler.execute();
    port.enqueue(e);
//    e2 = port.dequeue();*/

	std::cout <<"Start execution:"<<std::endl;
	scheduler.execute();
    //(*e2).printContent();
       	xmain();
	
}
*/
