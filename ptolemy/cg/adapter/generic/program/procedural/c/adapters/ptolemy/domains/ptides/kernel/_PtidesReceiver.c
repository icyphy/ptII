#include "$ModelName()__PtidesReceiver.h"

// Constructors of the basic receiver
struct PtidesReceiver* PtidesReceiver_New() {
	struct PtidesReceiver* newReceiver = malloc(sizeof(struct PtidesReceiver));
	if (PtidesReceiver == NULL) {
		fprintf(stderr, "Allocation error : PtidesReceiverr_New ($ModelName()__PtidesReceiver.c)\n");
		exit(-1);
	}
	PtidesReceiver_Init(newReceiver);
	newReceiver->free = PtidesReceiver_New_Free;

	return newReceiver;
}
struct PtidesReceiver PtidesReceiver_Create() {
	struct PtidesReceiver newReceiver;
	PtidesReceiver_Init(&newReceiver);
	newReceiver.free = PtidesReceiver_Free;
}

// Initialisation method
void PtidesReceiver_Init(struct PtidesReceiver* r) {
	DEReceiverInit(r);
	r->typeReceiver = PTIDESRECEIVER;

	r->put = PtidesReceiver_Put;
	r->putToReceiver = PtidesReceiver_PutToReceiver;
	r->remove = PtidesReceiver_Remove;
}

// Destructors
void PtidesReceiver_New_Free(struct PtidesReceiver* r) {
	if (r) {
		pblListFree(r->_tokens);
		free(r);
	}
}
void PtidesReceiver_Free(struct PtidesReceiver* r) {
	if (r) {
		pblListFree(r->_tokens);
	}
}

// Other methods
void PtidesReceiver_Put(struct PtidesReceiver* r, Token token) {
	// FIXME : it is not a relevant comparison
	if (token == NULL) {
		return;
	}
	(*(r->_director->_enqueueTriggerEvent))(r->container, token, r);
}
void PtidesReceiver_PutToReceiver(struct PtidesReceiver* r, Token token) {
	// FIXME : it is not a relevant comparison
	if (token != NULL) {
		pblListAdd(r->_tokens, &token);
	}
}
void PtidesReceiver_Remove(struct PtidesReceiver* r, Token token) {
	pblListRemoveElement(r->_tokens, &token);
}
