#ifndef DISPLAY_H
#define DISPLAY_H

#include "actor.h"
//#include "port.h"

class Display : public Actor{
public:
	Display(Scheduler* s) : Actor(s), input(this), output(this) {}
	virtual void initialize();
	virtual void fire();
	TypedPort<int> input, output;

};
inline void Display::initialize() {
	_s->registePort(&input);
}

inline void Display::fire() {
	if(input.hasToken()) {
		std::cout <<"fire the Display actor."<<std::endl;
		Event<int>* e2;
	    e2 = input.dequeue();
		e2->printContent();
	}
}
#endif
