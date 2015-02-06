/* In this file we have defined the structure of a FSMReceiver
 * It is derived from the Receiver and implements a FSM Receiver
 * class.
 *
 * @author : William Lucas
 */

#ifndef FSMRECEIVER_H_
#define FSMRECEIVER_H_

#include "_ptTypes.h"
#include "_TypedIOPort.h"
#include "_Receiver.h"

// Definition of the type of this receiver
#define FSMRECEIVER 3

struct FSMReceiver {
    // First we define the type of the Receiver
    int typeReceiver;

    // Here are the fields
    struct IOPort * container;

    // Here is the destructor
    void (*free)(struct FSMReceiver*);

    // Here are the methods
    Time (*getModelTime)(struct FSMReceiver*);
    void (*clear)(struct FSMReceiver*);
    PblList* (*elementList)(struct FSMReceiver*);
    Token* (*get)(struct FSMReceiver*);
    Token** (*getArray)(struct Receiver*, int);
    bool (*hasRoom)(struct FSMReceiver*);
    bool (*hasRoom1)(struct FSMReceiver*, int);
    bool (*hasToken)(struct FSMReceiver*);
    bool (*hasToken1)(struct FSMReceiver*, int);
    void (*put)(struct FSMReceiver*, Token*);
    void (*putArray)(struct Receiver*, Token**, int);
    void (*putArrayToAll)(struct Receiver*, Token**, int, PblList*);
    void (*putToAll)(struct Receiver*, Token* , PblList*);

    // New Members
    Token *_token;
};

struct FSMReceiver* FSMReceiver_New();
void FSMReceiver_Init(struct FSMReceiver*);
void FSMReceiver_New_Free(struct FSMReceiver* r);

void FSMReceiver_Clear(struct FSMReceiver* r);
PblList* FSMReceiver_ElementList(struct FSMReceiver* r);
Token* FSMReceiver_Get(struct FSMReceiver* r);
bool FSMReceiver_HasRoom(struct FSMReceiver* r);
bool FSMReceiver_HasRoom1(struct FSMReceiver* r, int numberOfTokens);
bool FSMReceiver_HasToken(struct FSMReceiver* r);
bool FSMReceiver_HasToken1(struct FSMReceiver* r, int numberOfTokens);
void FSMReceiver_Put(struct FSMReceiver* r, Token* token);

#endif /* FSMRECEIVER_H_ */
