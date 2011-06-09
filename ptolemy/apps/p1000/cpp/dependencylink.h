#ifndef DEPENDENCYLINK_H
#define DEPENDENCYLINK_H

//#include "eventqueue.h"
//#include "actor.h"
class Port;

class DependencyLink {
public:
	DependencyLink(Port* port, double dependency) : _port (port), _dependency (dependency) {}
	Port* getPort() {return _port;}
	double getDependency() {return _dependency;}
private:
	Port* _port;
	double _dependency;
};

#endif

