#ifndef EVENTQUEUE_H
#define EVENTQUEUE_H
#include <deque>
#include <iostream>

#include "event.h"


template <class T> class EventQueue {
public:
	typedef std::deque< Event< T > >  EVENTDEQUE;
	EventQueue() : _queue(){}
	void enqueue(Event<T> event);
	Event<T>* dequeue();
	Time getFirstEventTime();
	bool isEmpty() {return _queue.size() == 0;}
	void printContent();
private:
	EVENTDEQUE _queue;
};

template <class T> void
EventQueue<T>::enqueue(Event<T> event){
	_queue.push_back(event);
}

template <class T> Event<T>* 
EventQueue<T>::dequeue() {
	if (_queue.size() > 0) {
		Event<T>& front = _queue.front();
		_queue.pop_front();
		return &front;
	} else {
		return NULL;
	}
}

template <class T> Time 
EventQueue<T>::getFirstEventTime() {
	if (_queue.size() > 0) {
		Event<T>& front = _queue.front();
		return front.getTime();
	} else {
		Time time = {1000000, 0}; //Fixme: what is the keyword for Infinity?
		return time;
	}
}
template <class T> void 
EventQueue<T>::printContent() {
	typename EVENTDEQUE::iterator eventDeque;
	std::cout <<"The output is:"<<std::endl;
	for(eventDeque = _queue.begin();
           eventDeque != _queue.end();
           eventDeque++)
    {		   
	   ((Event<T>)*eventDeque).printContent();
	}
}

#endif
