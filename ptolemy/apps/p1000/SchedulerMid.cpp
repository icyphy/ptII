#include "scheduler.h"

#include "TriggeredClock.h"
#include "TriggerOut.h"
//class Scheduler; 
/*
void Scheduler::execute() {
	int i = 0;
	//std::cout <<"1 The size is:"<< _ports.size() <<std::endl;
	std::vector<Port*>::iterator portsIter;
	bool empty = false;
	while(!empty) {
        empty = true;
		for(portsIter = _ports.begin();
           portsIter != _ports.end();
           portsIter++)
        {
			Port* port = (Port*) *portsIter;
			if (!port->isQueueEmpty()) {
		        empty = false;
				port->enable();
				Actor* actor = port->getContainer();
				actor->fire();
			    //(*port).react();
			}
	    }
	}
}*/

void Scheduler::execute() {
	int i = 0;
	//std::cout <<"1 The size is:"<< _ports.size() <<std::endl;
	std::vector<Port*>::iterator portsIter;
	bool empty = false;
	while(!empty) {
        empty = true;
		std::vector<Actor*>::iterator actorsIter;
		for(portsIter = _ports.begin();
           portsIter != _ports.end();
           portsIter++)
        {
			Port* port = (Port*) *portsIter;
			if (!port->isQueueEmpty()) {
		        empty = false;
				if (port->hasMinimalEvent()) {
					//FIXME: should check whether the actor is already in the list
					//(for actors with more than one input port).
					_actors.push_back(port->getContainer());
				    port->enable();
				}
			}
		}
		for(actorsIter = _actors.begin();
           actorsIter != _actors.end();
           actorsIter++)
        {
			Actor* actor = (Actor*) *actorsIter;
			actor->fire();
		}
	}
}

/*template <class T> 
void Scheduler::postEvent(Token<T> token, TypedPort<T>* port, Time time){ 
    Event<T> e (token, time);
	port->enqueue(e);
}*/


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
