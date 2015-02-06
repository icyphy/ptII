/* In this file we have defined the structure of a PtidesReceiver
 * It is derived from the DEReceiver and implements a Ptides Receiver
 * class.
 *
 * @author : William Lucas
 */

#ifndef PTIDESRECEIVER_H_
#define PTIDESRECEIVER_H_

#include "_DEReceiver.h"
#include "_PtidesDirector.h"

// Definition of the type of this receiver
#define PTIDESRECEIVER 11
#define IS_PTIDESRECEIVER(p) ((p)->typeReceiver%100 == 11)

struct PtidesReceiver {
    // Members from parent class
    int typeReceiver;
    struct IOPort * container;
    void (*free)(struct PtidesReceiver*);
    Time (*getModelTime)(struct PtidesReceiver*);
    void (*clear)(struct PtidesReceiver*);
    PblList* (*elementList)(struct PtidesReceiver*);
    Token* (*get)(struct PtidesReceiver*);
    Token** (*getArray)(struct PtidesReceiver*, int);
    bool (*hasRoom)(struct PtidesReceiver*);
    bool (*hasRoom1)(struct PtidesReceiver*, int);
    bool (*hasToken)(struct PtidesReceiver*);
    bool (*hasToken1)(struct PtidesReceiver*, int);
    void (*put)(struct PtidesReceiver*, Token*);
    void (*putArray)(struct PtidesReceiver*, Token**, int);
    void (*putArrayToAll)(struct PtidesReceiver*, Token**, int, PblList*);
    void (*putToAll)(struct PtidesReceiver*, Token* , PblList*);

    PblList* _tokens;
    struct PtidesDirector* _director;

    // New Members
    void (*putToReceiver)(struct PtidesReceiver*, Token*);
    void (*remove)(struct PtidesReceiver*, Token*);
};

struct PtidesReceiver* PtidesReceiver_New();
void PtidesReceiver_Init(struct PtidesReceiver*);
void PtidesReceiver_New_Free(struct PtidesReceiver* r);

void PtidesReceiver_Put(struct PtidesReceiver* r, Token* token);
void PtidesReceiver_PutToReceiver(struct PtidesReceiver* r, Token* token);
void PtidesReceiver_Remove(struct PtidesReceiver* r, Token* token);

#endif /* PTIDESRECEIVER_H_ */
