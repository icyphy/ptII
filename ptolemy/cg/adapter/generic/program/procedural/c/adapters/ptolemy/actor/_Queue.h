/* In this file we have defined the structure of a Queue
 *
 * @author : William Lucas
 */

#ifndef QUEUE_H_
#define QUEUE_H_

#include <stdlib.h>
#include <errno.h>

#include "$ModelName()_types.h"

typedef struct Cell Cell;
struct Cell {
	void * content;
	Cell * next;
};

typedef struct Queue Queue;

struct Queue {
	Cell * first;
	Cell * last;
};

void QueueClear(Queue * r);
void * QueueGet(Queue * r);
void * QueueTake(Queue * r);
boolean QueueIsEmpty(Queue * r);
void QueuePut(Queue * r, void * t);

#endif /* DERECEIVER_H_ */
