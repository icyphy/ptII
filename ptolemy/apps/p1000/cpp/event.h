#ifndef EVENT_H
#define EVENT_H

#include <iostream>

#include "token.h"
// done by Jonathan to replace "time.h"
#include "hwTime.h"

template <class T> class Event {
public:
	//typedef Token<T> TOKENTYPE;
	Event(Token<T> token, Time time);
	Event(Token<T> data, Time time, bool isTimerEvent); 
	Token<T> getToken(void);
	Time getTime(void);
	bool isTimerEvent(void);
	void printContent(void);
private:
	Token<T> _data;
	Time _time;
	bool _isTimerEvent;
};

template <class T> 
Event<T>::Event(Token<T> token, Time time): _data (token), _time (time), _isTimerEvent (false) {}

template <class T> 
Event<T>::Event(Token<T> token, Time time, bool isTimerEvent): _data (token), _time (time), _isTimerEvent (isTimerEvent) {} 


template <class T> Token<T> 
Event<T>::getToken(void) { return _data;}

template <class T> Time 
Event<T>::getTime(void) {return _time;}
	
template <class T> bool 
Event<T>::isTimerEvent(void) {return _isTimerEvent;}
	
template <class T> void 
Event<T>::printContent(void) {
		std::cout << "token ";
		std::cout << _data.getValue();  
	    std::cout << ", time stamp ";
		std::cout << _time.ms;
		std::cout << " ms,";
        std::cout << _time.ns;
		std::cout << " ns"<<std::endl ;
}

//typedef Event<int> INTEVENT;
#endif 
