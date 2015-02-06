/* A struct which represent a generic I/O port with a type.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_TypedIOPort.c
 */

#ifndef TYPEDIOPORT_H_
#define TYPEDIOPORT_H_

#include <stdlib.h>
#include <stdbool.h>
#include <string.h>

#include "_ptTypes.h"
#include "_Receiver.h"
#include "_Actor.h"

#define TYPEDIOPORT 1

// Note that the order of fields in this struct should closely match
// the order in other files such as _IOPort.h,
// _PtidesMirrorPort.h, _PtidesPort.h
struct TypedIOPort {
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

    void (*free)(struct TypedIOPort*);

    void (*broadcast)(struct IOPort*, Token*);
    void (*broadcast1)(struct IOPort*, Token*, int, int);
    PblList* (*deepGetReceivers)(struct IOPort*);
    Token* (*get)(struct IOPort*, int);
    Token* (*get1)(struct IOPort*, int, int);
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
    void (*send1)(struct IOPort*, int, Token*, int);
    void (*sendInside)(struct IOPort*, int, Token*);
    void (*sendLocalInside)(struct IOPort*, int, Token*);

    // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct IOPort *);
    char *(*getName)(struct IOPort *);
    void (*setName)(struct IOPort *, char *);
#endif
    
#ifdef PTIDESDIRECTOR
    double delayOffset;
#endif

    // new members
    char (*getType)(struct TypedIOPort*);

    char _type;
};

struct TypedIOPort* TypedIOPort_New();
void TypedIOPort_Init(struct TypedIOPort* port);
void TypedIOPort_New_Free(struct TypedIOPort* port);

char TypedIOPort_GetType(struct TypedIOPort* port);

#endif
