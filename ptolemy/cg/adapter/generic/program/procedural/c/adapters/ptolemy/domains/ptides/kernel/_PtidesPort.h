/* In this file we have defined a struct PtidesPort.
 *
 * Source: $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesPort.h
 * @version: $Id$
 * @author : William Lucas
 */

#ifndef PTIDESPORT_H_
#define PTIDESPORT_H_

#include "_TypedIOPort.h"

/* Don't include _PtidesDirector.h here, this results in circular
   includes because _PtidesDirector.h includes _PtidesPort.h, which
   resulted in:

   commons/_PtidesDirector.h:78: warning: ‘struct PtidesPort’ declared inside parameter list
   commons/_PtidesDirector.h:78: warning: its scope is only this definition or declaration, which is probably not what you want
*/
//#include "_PtidesDirector.h"

#define PTIDESPORT 11
#define IS_PTIDESPORT(p) ((p)->typePort%100 == 11)

// Note that the order of fields in this struct should closely match
// the order in other files such as _IOPort.h, _TypedIOPort.h,
// _PtidesMirrorPort.h.
struct PtidesPort {
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

    void (*free)(struct PtidesPort*);

    void (*broadcast)(struct PtidesPort*, Token*);
    void (*broadcast1)(struct PtidesPort*, Token**, int, int);
    PblList* (*deepGetReceivers)(struct PtidesPort*);
    Token* (*get)(struct PtidesPort*, int);
    Token** (*get1)(struct PtidesPort*, int, int);
    int (*getChannelForReceiver)(struct PtidesPort*, struct Receiver*);
    Token* (*getInside)(struct PtidesPort*, int);
    PblList* (*getInsideReceivers)(struct PtidesPort*);
    Time (*getModelTime)(struct PtidesPort*, int);
    PblList* (*getReceivers)(struct PtidesPort*);
    PblList* (*getRemoteReceivers)(struct PtidesPort*);
    int (*getWidth)(struct PtidesPort*);
    int (*getWidthInside)(struct PtidesPort*);
    bool (*hasRoom)(struct PtidesPort*, int);
    bool (*hasRoomInside)(struct PtidesPort*, int);
    bool (*hasToken)(struct PtidesPort*, int);
    bool (*hasToken1)(struct PtidesPort*, int, int);
    bool (*hasTokenInside)(struct PtidesPort*, int);
    bool (*isInput)(struct PtidesPort*);
    bool (*isMultiport)(struct PtidesPort*);
    bool (*isOutput)(struct PtidesPort*);
    bool (*isOutsideConnected)(struct PtidesPort*);
    int (*numberOfSinks)(struct PtidesPort*);
    int (*numberOfSources)(struct PtidesPort*);
    void (*send)(struct PtidesPort*, int, Token*);
    void (*send1)(struct PtidesPort*, int, Token**, int);
    void (*sendInside)(struct PtidesPort*, int, Token*);

   // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct PtidesPort *);
    char *(*getName)(struct PtidesPort *);
    void (*setName)(struct PtidesPort *, char *);
#endif
    
#ifdef PTIDESDIRECTOR
    double delayOffset;
#endif

    char (*getType)(struct TypedIOPort*);
    char _type;

    // new members
    char* name;

    struct PtidesPort* _associatedPort;
    bool _settingAssociatedPort;

    void (*setAssociatedPort)(struct PtidesPort*, struct PtidesPort*);

    bool actuateAtEventTimestamp;
    Time deviceDelay;
    Time deviceDelayBound;
    bool isNetworkPort;
    Time networkDelayBound;
    Time platformDelayBound;
    Time sourcePlatformDelayBound;
    PblMap* _transmittedTokenTimestamps;
    PblMap* _transmittedTokenCnt;

    bool (*isActuatorPort)(struct PtidesPort*);
    bool (*isSensorPort)(struct PtidesPort*);
    bool (*isNetworkReceiverPort)(struct PtidesPort*);
    bool (*isNetworkTransmitterPort)(struct PtidesPort*);

    void (*_getTimeStampForToken)(struct PtidesPort*, Token*, Time*);
};

struct PtidesPort* PtidesPort_New();
void PtidesPort_Init(struct PtidesPort* port);
void PtidesPort_New_Free(struct PtidesPort* port);

void PtidesPort_SetAssociatedPort(struct PtidesPort* port, struct PtidesPort* port1);
bool PtidesPort_IsActuatorPort(struct PtidesPort* port);
bool PtidesPort_IsSensorPort(struct PtidesPort* port);
bool PtidesPort_IsNetworkReceiverPort(struct PtidesPort* port);
bool PtidesPort_IsNetworkTransmitterPort(struct PtidesPort* port);
void PtidesPort_Send(struct PtidesPort* port, int channelIndex, Token* token);

//Time* PtidesPort__GetTimeStampForToken(struct PtidesPort* port, Token t);
void PtidesPort__GetTimeStampForToken(struct PtidesPort* port, Token* t, Time* timestamps);


#endif
