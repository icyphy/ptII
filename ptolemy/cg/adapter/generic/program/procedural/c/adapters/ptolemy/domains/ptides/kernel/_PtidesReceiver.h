/* In this file we have defined the structure of a PtidesReceiver
 * It is derived from the DEReceiver and implements a Ptides Receiver
 * class.
 *
 * @author : William Lucas
 */

#ifndef PTIDESRECEIVER_H_
#define PTIDESRECEIVER_H_

#include "$ModelName()_types.h"
#include "$ModelName()__IOPort.h"
#include "$ModelName()__DEReceiver.h"

// Definition of the type of this receiver
#define PTIDESRECEIVER 11

struct PtidesReceiver {
	// Members from parent class
	int typeReceiver;
	struct IOPort * container;
	void (*free)(struct Receiver*);
	int (*getModelTime)(struct Receiver*);
	void (*clear)(struct Receiver*);
	PblList* (*elementList)(struct Receiver*);
	Token (*get)(struct Receiver*);
	bool (*hasRoom)(struct Receiver*);
	bool (*hasRoom1)(struct Receiver*, int);
	bool (*hasToken)(struct Receiver*);
	bool (*hasToken1)(struct Receiver*, int);
	void (*put)(struct Receiver*, Token);
	void (*putArray)(struct Receiver*, Token[], int , int);
	void (*putArrayToAll)(struct Receiver*, Token[], int, int, struct Receiver*[], int);
	void (*putToAll)(struct Receiver*, Token , struct Receiver*[], int);

	PblList* _tokens;
	DEDirector* _director;

	// New Members
	void (*putToReceiver)(struct PtidesReceiver*, Token);
	void (*remove)(struct PtidesReceiver*, Token);
};

struct DEReceiver* DEReceiver_New();
struct DEReceiver DEReceiver_Create();
void DEReceiver_Init(struct DEReceiver*);
void DEReceiver_New_Free(struct DEReceiver* r);
void DEReceiver_Free(struct DEReceiver* r);


#endif /* PTIDESRECEIVER_H_ */
