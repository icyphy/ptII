/* A C implementation of the PtidesMirrorPort Java class.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesMirrorPort.h
 */

#ifndef PTIDESMIRRORPORT_H_
#define PTIDESMIRRORPORT_H_

#include "_TypedIOPort.h"
#include "_PtidesPlatformDirector.h"

#define PTIDESMIRRORPORT 21
#define IS_PTIDESMIRRORPORT(p) ((p)->typePort%100 == 21)

// Note that the order of fields in this struct should closely match
// the order in other files such as _IOPort.h, _TypedIOPort.h,
// _PtidesPort.h
struct PtidesMirrorPort {
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

    void (*free)(struct PtidesMirrorPort*);

    void (*broadcast)(struct PtidesMirrorPort*, Token);
    void (*broadcast1)(struct PtidesMirrorPort*, Token*, int, int);
    PblList* (*deepGetReceivers)(struct PtidesMirrorPort*);
    Token (*get)(struct PtidesMirrorPort*, int);
    Token* (*get1)(struct PtidesMirrorPort*, int, int);

    //MEMORY_FIX: getBoolean, getInt, getDouble, which free the Token and return payload
    #ifdef TYPE_Boolean
    boolean (*getBoolean)(struct IOPort*, int);
    #endif
    #ifdef TYPE_Int
    int (*getInt)(struct IOPort*, int);
    #endif
    #ifdef TYPE_Double
    double (*getDouble)(struct IOPort*, int);
    #endif

    int (*getChannelForReceiver)(struct PtidesMirrorPort*, struct Receiver*);
    Token (*getInside)(struct PtidesMirrorPort*, int);
    PblList* (*getInsideReceivers)(struct PtidesMirrorPort*);
    Time (*getModelTime)(struct PtidesMirrorPort*, int);
    PblList* (*getReceivers)(struct PtidesMirrorPort*);
    PblList* (*getRemoteReceivers)(struct PtidesMirrorPort*);
    int (*getWidth)(struct PtidesMirrorPort*);
    int (*getWidthInside)(struct PtidesMirrorPort*);
    bool (*hasRoom)(struct PtidesMirrorPort*, int);
    bool (*hasRoomInside)(struct PtidesMirrorPort*, int);
    bool (*hasToken)(struct PtidesMirrorPort*, int);
    bool (*hasToken1)(struct PtidesMirrorPort*, int, int);
    bool (*hasTokenInside)(struct PtidesMirrorPort*, int);
    bool (*isInput)(struct PtidesMirrorPort*);
    bool (*isMultiport)(struct PtidesMirrorPort*);
    bool (*isOutput)(struct PtidesMirrorPort*);
    bool (*isOutsideConnected)(struct PtidesMirrorPort*);
    int (*numberOfSinks)(struct PtidesMirrorPort*);
    int (*numberOfSources)(struct PtidesMirrorPort*);
    void (*send)(struct PtidesMirrorPort*, int, Token);
    void (*send1)(struct PtidesMirrorPort*, int, Token*, int);
    void (*sendInside)(struct PtidesMirrorPort*, int, Token);

   // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct PtidesMirrorPort *);
    char *(*getName)(struct PtidesMirrorPort *);
    void (*setName)(struct PtidesMirrorPort *, char *);
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

    void (*setAssociatedPort)(struct PtidesMirrorPort*, struct PtidesMirrorPort*);
};

//MEMORY_FIX: getBoolean, getInt, getDouble, which free the Token and return payload
#ifdef TYPE_Boolean
boolean IOPort_GetBoolean(struct IOPort* port, int channelIndex);
#endif
#ifdef TYPE_Int
int IOPort_GetInt(struct IOPort* port, int channelIndex);
#endif
#ifdef TYPE_Double
double IOPort_GetDouble(struct IOPort* port, int channelIndex);
#endif

struct PtidesMirrorPort* PtidesMirrorPort_New();
void PtidesMirrorPort_Init(struct PtidesMirrorPort* port);
void PtidesMirrorPort_New_Free(struct PtidesMirrorPort* port);

void PtidesMirrorPort_SetAssociatedPort(struct PtidesMirrorPort* port, struct PtidesMirrorPort* port1);

#endif
