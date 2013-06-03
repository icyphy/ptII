/* In this file we have defined a struct IOPort which represent a generic I/O port
 *
 * @author : William Lucas
 */

#ifndef IOPORT_H_
#define IOPORT_H_

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

#include "$ModelName()_types.h"
#include "$ModelName()__Receiver.h"
#include "$ModelName()__Actor.h"

struct IOPort {
	Actor * containingActor;
	char * name;
	char * type;
	bool isInput;
	bool isOutput;
	bool isMultiport;
	Receiver * receivers;
	Receiver ** farReceivers;
	int width;
};

void IOPortSet(IOPort * port, Actor * containingActor, char * name, char * type,
		bool isInput, bool isOutput, bool isMultiport, int widthInside, int widthOutside);
void IOPortDelete(IOPort * port);

#endif
