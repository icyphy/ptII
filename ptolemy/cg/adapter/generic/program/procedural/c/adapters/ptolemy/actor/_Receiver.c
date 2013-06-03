#include "$ModelName()__Receiver.h"

// Sets the remoteReceiver field to the receiver in parameter
void ReceiverSetReceiver(Receiver * r, IOPort * port) {
	if (r == NULL) {
		perror("Trying to set a NULL receiver !");
		exit(1);
	}
	r->port = port;
	return;
}
// Sets the remoteReceiver field to the far receiver in parameter
void ReceiverSetRemoteFarReceiver(Receiver ** r, Receiver * remote) {
	*r = remote;
	return;
}

// Clears the queue
void ReceiverClear(Receiver * r) {
	if (r == NULL) {
		perror("Trying to clear a NULL receiver !");
		exit(1);
	}
	QueueClear(&(r->events));
	return;
}

// Returns the first element of the queue
Token ReceiverGet(Receiver * r) {
	if (!ReceiverHasToken(r))
		return emptyToken;

	Token * p_result = (Token*)QueueTake(&(r->events));
	Token result = *p_result;
	if (p_result != NULL)
		free(p_result);
	return result;
}

// Returns a boolean which says if there are token in the receiver
bool ReceiverHasToken(Receiver * r) {
	if (r == NULL) {
		return false;
	}
	return (!QueueIsEmpty(&(r->events)));
}

// Puts an event in the queue
void ReceiverPut(Receiver * r, Token t) {
	if (r == NULL) {
		perror("Trying to put an event in a NULL receiver !");
		exit(1);
	}
	// FIXME : need to free this malloc on get
	Token * toPut = malloc(sizeof(Token));
	if (toPut == NULL) {
		perror("Allocation Error (ReceiverPut)");
		exit(1);
	}
	*toPut = t;
	QueuePut(&(r->events), toPut);
	return;
}
