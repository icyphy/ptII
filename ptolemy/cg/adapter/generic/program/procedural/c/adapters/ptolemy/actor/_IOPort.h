/* In this file we have defined a struct IOPort which represent a generic I/O port
 *
 * @author : William Lucas
 */

#ifndef IOPORT_H_
#define IOPORT_H_

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

#include "_ptTypes.h"
#include "_Receiver.h"
#include "_Actor.h"

#define IOPORT 0

struct IOPort {
    int typePort;

    struct Actor* container;

    bool _isInsideConnected;
    bool _isOutsideConnected;
    bool _isInput;
    bool _isOutput;
    bool _isMultiport;
    int _width;
    int _insideWidth;
    PblList* _farReceivers;
    PblList* _localReceivers;
    PblList* _localInsideReceivers;
    PblList* _insideReceivers;
    int _numberOfSinks;
    int _numberOfSources;

    void (*free)(struct IOPort*);

    void (*broadcast)(struct IOPort*, Token*);
    void (*broadcast1)(struct IOPort*, Token**, int, int);
    PblList* (*deepGetReceivers)(struct IOPort*);
    Token* (*get)(struct IOPort*, int);
    Token** (*get1)(struct IOPort*, int, int);
    int (*getChannelForReceiver)(struct IOPort*, struct Receiver*);
    Token* (*getInside)(struct IOPort*, int);
    PblList* (*getInsideReceivers)(struct IOPort*);
    Time (*getModelTime)(struct IOPort*, int);
    PblList* (*getReceivers)(struct IOPort*);
    PblList* (*getRemoteReceivers)(struct IOPort*);
    int (*getWidth)(struct IOPort*);
    int (*getWidthInside)(struct IOPort*);
    bool (*hasRoom)(struct IOPort*, int);
    bool (*hasRoomInside)(struct IOPort*, int);
    bool (*hasToken)(struct IOPort*, int);
    bool (*hasToken1)(struct IOPort*, int, int);
    bool (*hasTokenInside)(struct IOPort*, int);
    bool (*isInput)(struct IOPort*);
    bool (*isMultiport)(struct IOPort*);
    bool (*isOutput)(struct IOPort*);
    bool (*isOutsideConnected)(struct IOPort*);
    int (*numberOfSinks)(struct IOPort*);
    int (*numberOfSources)(struct IOPort*);
    void (*send)(struct IOPort*, int, Token*);
    void (*send1)(struct IOPort*, int, Token**, int);
    void (*sendInside)(struct IOPort*, int, Token*);
    void (*sendLocalInside)(struct IOPort*, int, Token*);

#ifdef PTIDESDIRECTOR
    double delayOffset;
#endif
};

struct IOPort* IOPort_New();
void IOPort_Init(struct IOPort* port);
void IOPort_New_Free(struct IOPort* port);

void IOPort_Broadcast(struct IOPort* port, Token* token);
void IOPort_Broadcast1(struct IOPort* port, Token** tokenArray, int sizeTokenArray, int vectorLength);
PblList* IOPort_DeepGetReceivers(struct IOPort* port);
Token* IOPort_Get(struct IOPort* port, int channelIndex);
Token** IOPort_Get1(struct IOPort* port, int channelIndex, int vectorLength);
int IOPort_GetChannelForReceiver(struct IOPort* port, struct Receiver* receiver);
Token* IOPort_GetInside(struct IOPort* port, int channelIndex);
PblList* IOPort_GetInsideReceivers(struct IOPort* port);
Time IOPort_GetModelTime(struct IOPort* port, int channelIndex);
PblList* IOPort_GetReceivers(struct IOPort* port);
PblList* IOPort_GetRemoteReceivers(struct IOPort* port);
int IOPort_GetWidth(struct IOPort* port);
int IOPort_GetWidthInside(struct IOPort* port);
bool IOPort_HasRoom(struct IOPort* port, int channelIndex);
bool IOPort_HasRoomInside(struct IOPort* port, int channelIndex);
bool IOPort_HasToken(struct IOPort* port, int channelIndex);
bool IOPort_HasToken1(struct IOPort* port, int channelIndex, int tokens);
bool IOPort_HasTokenInside(struct IOPort* port, int channelIndex);
bool IOPort_IsInput(struct IOPort* port);
bool IOPort_IsMultiport(struct IOPort* port);
bool IOPort_IsOutput(struct IOPort* port);
bool IOPort_IsOutsideConnected(struct IOPort* port);
int IOPort_NumberOfSinks(struct IOPort* port);
int IOPort_NumberOfSources(struct IOPort* port);
void IOPort_Send(struct IOPort* port, int channelIndex, Token* token);
void IOPort_Send1(struct IOPort* port, int channelIndex, Token** tokenArray, int vectorLength);
void IOPort_SendInside(struct IOPort* port, int channelIndex, Token* token);
void IOPort_SendLocalInside(struct IOPort* port, int channelIndex, Token* token);

#endif
