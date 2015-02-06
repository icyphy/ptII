/* In this file we have defined the structure of a receiver
 * A receiver is just defined by its container in this base
 * class.
 *
 * @author : William Lucas
 */

#ifndef RECEIVER_H_
#define RECEIVER_H_

#include "_ptTypes.h"
#include "_IOPort.h"
#include "_CompositeActor.h"
#include "_Director.h"

// Definition of the type of this receiver
#define RECEIVER 0

struct Receiver {
    // First we define the type of the Receiver
    int typeReceiver;

    // Here are the fields
    struct IOPort * container;

    // Here is the destructor
    void (*free)(struct Receiver*);

    // Here are the methods
    Time (*getModelTime)(struct Receiver*);
    void (*clear)(struct Receiver*);
    PblList* (*elementList)(struct Receiver*);
    Token* (*get)(struct Receiver*);
    Token** (*getArray)(struct Receiver*, int);
    bool (*hasRoom)(struct Receiver*);
    bool (*hasRoom1)(struct Receiver*, int);
    bool (*hasToken)(struct Receiver*);
    bool (*hasToken1)(struct Receiver*, int);
    void (*put)(struct Receiver*, Token*);
    void (*putArray)(struct Receiver*, Token**, int);
    void (*putArrayToAll)(struct Receiver*, Token**, int, PblList*);
    void (*putToAll)(struct Receiver*, Token* , PblList*);
};

struct Receiver* Receiver_New();
void Receiver_Init(struct Receiver*);
void Receiver_New_Free(struct Receiver* r);

Time Receiver_GetModelTime(struct Receiver* r);
void Receiver_PutArray(struct Receiver* r, Token** tokenArray, int numberOfTokens);
void Receiver_PutArrayToAll(struct Receiver* r, Token** tokenArray,
                            int numberOfTokens, PblList* receivers);
void Receiver_PutToAll(struct Receiver* r, Token* token, PblList* receivers);


#endif /* RECEIVER_H_ */
