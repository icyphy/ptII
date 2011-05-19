#ifndef PORT_H
#define PORT_H
#include <vector>
//#include "eventqueue.h"
#include "hwTime.h"
//#include "time.h"
#include "dependencylink.h"
class Actor;

class Port {
public:
	Port(Actor* actor) : _hasToken (false), _isRealTime (false), _container (actor), _links () {}
	Port(Actor* actor, bool flag): _hasToken (false), _isRealTime (flag), _container (actor) {};
	void connect(Port *port) {_connectedPort = port;}
	void disable() {_hasToken = false;}
	void enable() {_hasToken = true;}
	bool hasToken() {return _hasToken;}
	Actor* getContainer() {return _container;}
	bool hasMinimalEvent();
	/**
	derived class should override this method to return the earliest time in the contained event queue.
	*/
	virtual Time getEarliestTime(){Time time = {0, 0}; return time;}
	virtual bool isQueueEmpty(){return false;}
    virtual bool isRealTime(){return _isRealTime;}
	void setDependencyLink(DependencyLink* link) {_links.push_back(link);}
	//template <class T> virtual void enqueue(Event<T> e) {};
	//template <class T> void send(Event<T> event) {_connectedPort->enqueue(event);}
protected:
    Port* _connectedPort;
private:
	Actor* _container;
	bool _hasToken;
	bool _isRealTime;
	std::vector <DependencyLink*> _links;

	bool _isGreater(Time thisTime, Time dTime, double dependency);
};

inline bool Port::hasMinimalEvent() {
    std::vector<DependencyLink*>::iterator linksIter;
	bool hasMinimal = true;
	Time thisTime = getEarliestTime();
	for(linksIter = _links.begin();
           linksIter != _links.end();
           linksIter++)
    {
	    DependencyLink* link = (DependencyLink*) *linksIter;
		Port* dPort = link->getPort();
		Time dTime = dPort->getEarliestTime();
		double dependency = link->getDependency();
		if (_isGreater(thisTime, dTime, dependency)) {
			hasMinimal = false;
			break;
		}
	}
	return hasMinimal;
}

inline bool Port::_isGreater(Time thisTime, Time dTime, double dependency){
    if ((thisTime.ms + thisTime.ns * 0.000001) + dependency > 
		(dTime.ms + dTime.ns * 0.000001) ) {
		return true;
	}
	return false;
}
#endif
