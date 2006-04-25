#ifndef TYPEDPORT_H
#define TYPEDPORT_H

#include "port.h"
#include "eventqueue.h"

//class Port;

template <class T> class TypedPort : public Port {
public:
	TypedPort(Actor* actor);
	TypedPort(Actor* actor, bool flag);
	//virtual void react() override;
	void enqueue(Event<T> e);
	Event<T>* dequeue();
	virtual Time getEarliestTime();
	virtual bool isQueueEmpty() {return _portEventQueue.isEmpty();};
	virtual bool isRealTime();
	void send(Event<T> event) {((TypedPort<T>*)_connectedPort)->enqueue(event);}
private:	
	EventQueue<T> _portEventQueue;
	//DependencyLink<T>* _links[];
	bool _isRealTime;
};

template <class T> 
TypedPort<T>::TypedPort(Actor* actor) : Port(actor), _portEventQueue(){}

template <class T> 
TypedPort<T>::TypedPort(Actor* actor, bool flag) : Port(actor, flag), _portEventQueue(){}

/*template <class T> void
TypedPort<T>::react() {
    Event<T>* e2;
	e2 = dequeue();
	e2->printContent();
}*/
template <class T> void
TypedPort<T>::enqueue(Event<T> e) {
	_portEventQueue.enqueue(e);
}

template <class T> Event<T>* 
TypedPort<T>::dequeue() {
	disable();
	return _portEventQueue.dequeue();
}

template <class T> Time 
TypedPort<T>::getEarliestTime() {
	return _portEventQueue.getFirstEventTime();
}
template <class T> bool 
TypedPort<T>::isRealTime() {
	return _isRealTime;
}

#endif
