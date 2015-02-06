/* In this file we have defined the structure of a DEReceiver
 * It is derived from the Receiver and implements a DE Receiver
 * class.
 *
 * @author : William Lucas
 */

#ifndef DERECEIVER_H_
#define DERECEIVER_H_

#include "_ptTypes.h"
#include "_TypedIOPort.h"
#include "_DEReceiver.h"
#include "_DEDirector.h"

// Definition of the type of this receiver
#define DERECEIVER 1

struct DEReceiver {
    // Members from parent class
    int typeReceiver;
    struct IOPort * container;
    void (*free)(struct DEReceiver*);
    Time (*getModelTime)(struct Receiver*);
    void (*clear)(struct DEReceiver*);
    PblList* (*elementList)(struct DEReceiver*);
    Token* (*get)(struct DEReceiver*);
    Token** (*getArray)(struct Receiver*, int);
    bool (*hasRoom)(struct DEReceiver*);
    bool (*hasRoom1)(struct DEReceiver*, int);
    bool (*hasToken)(struct DEReceiver*);
    bool (*hasToken1)(struct DEReceiver*, int);
    void (*put)(struct DEReceiver*, Token*);
    void (*putArray)(struct Receiver*, Token**, int);
    void (*putArrayToAll)(struct Receiver*, Token**, int, PblList*);
    void (*putToAll)(struct Receiver*, Token* , PblList*);

    // New Members
    PblList* _tokens;
    struct DEDirector* _director;
};

struct DEReceiver* DEReceiver_New();
void DEReceiver_Init(struct DEReceiver*);
void DEReceiver_New_Free(struct DEReceiver* r);

void DEReceiver_Clear(struct DEReceiver* r);
PblList* DEReceiver_ElementList(struct DEReceiver* r);
Token* DEReceiver_Get(struct DEReceiver* r);
bool DEReceiver_HasRoom(struct DEReceiver* r);
bool DEReceiver_HasRoom1(struct DEReceiver* r, int numberOfTokens);
bool DEReceiver_HasToken(struct DEReceiver* r);
bool DEReceiver_HasToken1(struct DEReceiver* r, int numberOfTokens);
void DEReceiver_Put(struct DEReceiver* r, Token* token);

#endif /* DERECEIVER_H_ */
