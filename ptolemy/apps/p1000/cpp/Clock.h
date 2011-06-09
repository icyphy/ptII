#ifndef CLOCK_H
#define CLOCK_H

#include "actor.h"
//#include "port.h"

class Clock : public Actor{
public:
	Clock(Scheduler* s) : Actor(s), fireAt(this), output(this) {}
	virtual void initialize();
	virtual void fire();
	void setEndTime(Time endTime);
	TypedPort<int> fireAt, output;
private:
	Time _time;
    Time _endTime;
};
inline void Clock::initialize() {
	_s->registePort(&fireAt);
	Token<int> token (1);
	_time.ms = 100;
	_time.ns = 0;
	_s->postEvent<int> (token, &fireAt, _time);

	//Event<int>* e2 = input.dequeue();

	//std::cout <<"The output is:"<<std::endl;
    //(*e2).printContent();
}

inline void Clock::fire() {
	if(fireAt.hasToken()) {
		std::cout <<"fire the Clock actor."<<std::endl;
		Event<int>* e2;
	    e2 = fireAt.dequeue();
		output.send(*e2);
		if (_time.ms < _endTime.ms) {
		    _time.ms += 100;
		    Token<int> token (1);
		    _s->postEvent<int> (token, &fireAt, _time);
		//e2->printContent();
		}
	}
}

inline void Clock::setEndTime(Time endTime) {
	_endTime = endTime;
}
#endif
