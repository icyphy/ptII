#ifndef ACTOR_H
#define ACTOR_H

#include "scheduler.h"

class Actor {
public:
	Actor(Scheduler* s) : _s (s) {}
	virtual void fire() {};
	virtual void initialize(){};
	virtual void stop() {};
//private:	
	Scheduler* _s;
};

#endif

