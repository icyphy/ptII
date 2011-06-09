#ifndef MERGE_H
#define MERGE_H

#include "actor.h"
//#include "port.h"

class Merge : public Actor{
public:
	Merge(Scheduler* s) : Actor(s), input1(this), input2(this), output(this) {}
	virtual void initialize() override;
	virtual void fire() override;
	TypedPort<int> input1, input2, output;

};
void Merge::initialize() {
	_s->registePort(&input1);
	_s->registePort(&input2);
}

void Merge::fire() {
	if(input1.hasToken()) {
		std::cout <<"fire the Merge actor, get input from input1."<<std::endl;
		Event<int>* e2;
	    e2 = input1.dequeue();
		e2->printContent();
	} else if (input2.hasToken()) {
		std::cout <<"fire the Merge actor, get input from input2."<<std::endl;
		Event<int>* e2;
	    e2 = input2.dequeue();
		e2->printContent();
	}
}
#endif
