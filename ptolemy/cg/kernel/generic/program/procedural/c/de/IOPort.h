/* In this file we have defined a struct IOPort which represent a generic I/O port
 *
 * @author : William Lucas
 */

#ifndef IOPORT_H_
#define IOPORT_H_

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <errno.h>
#include <string.h>
#include "DEEvent.h"

typedef struct IOPort IOPort;
typedef struct DEEvent DEEvent;

struct IOPort {
	char * name;
	char * type;
	bool isInput;
	bool isMultiport;
	DEEvent ** eventsToSend;
	int width;
};


IOPort * newIOPort();
IOPort * newIOPortWithParam(char* name, char* type,
		bool isInput, bool isMultiport, int width);
void IOPortDelete(IOPort * port);

#endif
