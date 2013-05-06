#include "../includes/DEReceiver.h"

// Simply deletes properly all the elements in the queue
void DEReceiverClear(DEReceiver * r) {
	if (r == NULL)
		return;
	if (r->first == NULL)
		return;
	if (r->first == r->last)
		free(r->first);
	else
	{
		TokenCell * lastToken = NULL;
		TokenCell * t = NULL;
		for(t = r->first ; t != r->last ; t = t->next) {
			free(lastToken);
			lastToken = t;
		}
		free(lastToken);
	}
	return;
}

// Returns the first element of the queue
Token DEReceiverGet(DEReceiver * r) {
	if (!DEReceiverHasToken(r))
		return emptyToken;

	TokenCell * result = r->first;
	r->first = result->next;
	// In the case we removed the last element of the queue
	if (r->first == NULL)
		r->last = NULL;

	return result->token;
}

// Returns a boolean which says if there are token in the receiver
bool DEReceiverHasToken(DEReceiver * r) {
	return (r != NULL && r->first != NULL);
}

// Puts a clone of the token t in the queue
void DEReceiverPut(DEReceiver * r, Token t) {
	if (r == NULL) {
		r = malloc(sizeof(DEReceiver));
		if (r == NULL) {
			perror("Allocation error (DEReceiverPut)");
			exit(1);
		}
		TokenCell * newToken = malloc(sizeof(TokenCell));
		if (newToken == NULL) {
			perror("Allocation error (DEReceiverPut)");
			exit(1);
		}
		newToken->token = t;
		r->first = r->last = newToken;
	}
	else if (!DEReceiverHasToken(r)) {
		TokenCell * newToken = malloc(sizeof(TokenCell));
		if (newToken == NULL) {
			perror("Allocation error (DEReceiverPut)");
			exit(1);
		}
		newToken->token = t;
		newToken->next = NULL;
		r->first = r->last = newToken;
	}
	else {
		TokenCell * newToken = malloc(sizeof(TokenCell));
		if (newToken == NULL) {
			perror("Allocation error (DEReceiverPut)");
			exit(1);
		}
		newToken->token = t;
		newToken->next = NULL;
		r->last->next = newToken;
		r->last = newToken;
	}
	return;
}
