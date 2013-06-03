#include "$ModelName()__Queue.h"

// Simply deletes properly all the elements in the queue
void QueueClear(Queue * r) {
	if (r == NULL)
		return;
	if (r->first == NULL)
		return;
	if (r->first == r->last)
		free(r->first);
	else
	{
		Cell * lastToken = NULL;
		Cell * t = NULL;
		for(t = r->first ; t != r->last ; t = t->next) {
			free(lastToken);
			lastToken = t;
		}
		free(lastToken);
	}
	return;
}

// Returns the first element of the queue
void * QueueGet(Queue * r) {
	if (QueueIsEmpty(r))
		return NULL;

	Cell * result = r->first;

	return result->content;
}

// Returns the first element of the queue
void * QueueTake(Queue * r) {
	if (QueueIsEmpty(r))
		return NULL;

	Cell * result = r->first;
	r->first = result->next;
	// In the case we removed the last element of the queue
	if (r->first == NULL)
		r->last = NULL;

	// FIXME : Shouldn't we free the Cell ?
	return result->content;
}

// Returns a boolean which says if the queue is empty
boolean QueueIsEmpty(Queue * r) {
	return (r == NULL || r->first == NULL);
}

// Puts a clone of the content in the queue
void QueuePut(Queue * r, void * t) {
	if (QueueIsEmpty(r)) {
		if (r == NULL) {
			r = malloc(sizeof(Queue));
			if (r == NULL) {
				perror("Allocation error (QueuePut)");
				exit(1);
			}
		}
		Cell * newCell = malloc(sizeof(Cell));
		if (newCell == NULL) {
			perror("Allocation error (QueuePut)");
			exit(1);
		}
		newCell->content = t;
		newCell->next = NULL;
		r->first = r->last = newCell;
	}
	else {
		Cell * newCell = malloc(sizeof(Cell));
		if (newCell == NULL) {
			perror("Allocation error (QueuePut)");
			exit(1);
		}
		newCell->content = t;
		newCell->next = NULL;
		r->last->next = newCell;
		r->last = newCell;
	}
	return;
}
