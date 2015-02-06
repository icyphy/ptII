/* In this file we have defined the structure of a SDFReceiver
 * It is derived from the Receiver and implements a SDF Receiver
 * class.
 *
 * @author : William Lucas
 */

#ifndef SDFRECEIVER_H_
#define SDFRECEIVER_H_

#include "_ptTypes.h"
#include "_TypedIOPort.h"
#include "_Receiver.h"

// Definition of the type of this receiver
#define SDFRECEIVER 2

struct SDFReceiver {
    // First we define the type of the Receiver
    int typeReceiver;

    // Here are the fields
    struct IOPort * container;

    // Here is the destructor
    void (*free)(struct SDFReceiver*);

    // Here are the methods
    Time (*getModelTime)(struct SDFReceiver*);
    void (*clear)(struct SDFReceiver*);
    PblList* (*elementList)(struct SDFReceiver*);
    Token* (*get)(struct SDFReceiver*);
    Token** (*getArray)(struct Receiver*, int);
    bool (*hasRoom)(struct SDFReceiver*);
    bool (*hasRoom1)(struct SDFReceiver*, int);
    bool (*hasToken)(struct SDFReceiver*);
    bool (*hasToken1)(struct SDFReceiver*, int);
    void (*put)(struct SDFReceiver*, Token*);
    void (*putArray)(struct Receiver*, Token*, int);
    void (*putArrayToAll)(struct Receiver*, Token*, int, PblList*);
    void (*putToAll)(struct Receiver*, Token , PblList*);

    // New Members
    int _waitingTokens;
    PblList* _queue;
};

struct SDFReceiver* SDFReceiver_New();
void SDFReceiver_Init(struct SDFReceiver*);
void SDFReceiver_New_Free(struct SDFReceiver* r);

void SDFReceiver_Clear(struct SDFReceiver* r);
PblList* SDFReceiver_ElementList(struct SDFReceiver* r);
Token* SDFReceiver_Get(struct SDFReceiver* r);
bool SDFReceiver_HasRoom(struct SDFReceiver* r);
bool SDFReceiver_HasRoom1(struct SDFReceiver* r, int numberOfTokens);
bool SDFReceiver_HasToken(struct SDFReceiver* r);
bool SDFReceiver_HasToken1(struct SDFReceiver* r, int numberOfTokens);
void SDFReceiver_Put(struct SDFReceiver* r, Token* token);

#endif /* SDFRECEIVER_H_ */
