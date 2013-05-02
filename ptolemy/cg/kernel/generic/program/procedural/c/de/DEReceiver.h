/* In this file we have defined the structure of a DE receiver
 * A DE receiver is just a FIFO of tokens from which we can put
 * or get events
 *
 * @author : William Lucas
 */

#ifndef DERECEIVER_H_
#define DERECEIVER_H_

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include "types.h"

typedef struct TokenCell TokenCell;
struct TokenCell {
	Token token;
	TokenCell * next;
};

typedef struct DEReceiver DEReceiver;
struct DEReceiver {
	TokenCell * first;
	TokenCell * last;
};

void DEReceiverClear(DEReceiver * r);
Token DEReceiverGet(DEReceiver * r);
bool DEReceiverHasToken(DEReceiver * r);
void DEReceiverPut(DEReceiver * r, Token t);

#endif /* DERECEIVER_H_ */
