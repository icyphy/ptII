#include <vector>
#include <iostream>

#include "scheduler.h"
#include "actor.h"
#include "port.h"
// Jonathan changed from clock.h and display.h
#include "Clock.h"
#include "Display.h"

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
	while(1) {
	  //	while(!empty) {
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

template <class T> 
void Scheduler::postEvent(Token<T> token, TypedPort<T>* port, Time time){ 
    Event<T> e (token, time);
	port->enqueue(e);
}

// removed to allow compilation...

//int main() {
//    typedef int T;
//	Scheduler scheduler;
//	Clock clock (&scheduler);
//	Display display(&scheduler);
//	clock.output.connect(&(display.input));
//	Time endTime = {1000, 0};
//	clock.setEndTime(endTime);
//
//	DependencyLink* link = new DependencyLink(&(clock.fireAt), 300.0);
//	display.input.setDependencyLink(link);
//	clock.initialize();
//	display.initialize();
//
//	/*Token<T> token (1);
//	Time time = {100, 10};
//	Event<T> e (token, time);
//	Event<T>* e2;
//	Scheduler scheduler;
//
//	Port<T> port (false);
//	scheduler.registePort(&port);
//	scheduler.postEvent(token, &port, time);
//	scheduler.execute();
//    port.enqueue(e);
//    e2 = port.dequeue();*/
//
//	std::cout <<"Start execution:"<<std::endl;
//	scheduler.execute();
//    //(*e2).printContent();
//	
//	
//}
