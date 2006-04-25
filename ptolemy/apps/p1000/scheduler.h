#ifndef SCHEDULER_H
#define SCHEDULER_H
#include <vector>
#include <iostream>

#include "typedport.h"

class Actor;

class Scheduler {
public:
	Scheduler() : _ports(), _actors(){}
	//template <class T> void registePort(Port<T>* port) {_ports.push_back(port);}
	void registePort(Port* port) {_ports.push_back(port);}
	void initialize(){};
	void execute();
	template <class T> void postEvent(Token<T> token, TypedPort<T>* port, Time time);
	int fd;
private:
	void _processEarliestEvent() {};
	std::vector <Port*> _ports;
    std::vector <Actor*> _actors;
};



#endif 
