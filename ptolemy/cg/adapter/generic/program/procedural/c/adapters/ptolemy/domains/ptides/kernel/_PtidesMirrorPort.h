/* In this file we have defined a struct PtidesMirrorPort
 *
 * @author : William Lucas
 */

#ifndef PTIDESMIRRORPORT_H_
#define PTIDESMIRRORPORT_H_

#include "_TypedIOPort.h"
#include "_PtidesPlatformDirector.h"

#define PTIDESMIRRORPORT 21
#define IS_PTIDESMIRRORPORT(p) ((p)->typePort%100 == 21)

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

struct PtidesMirrorPort* PtidesMirrorPort_New();
void PtidesMirrorPort_Init(struct PtidesMirrorPort* port);
void PtidesMirrorPort_New_Free(struct PtidesMirrorPort* port);

void PtidesMirrorPort_SetAssociatedPort(struct PtidesMirrorPort* port, struct PtidesMirrorPort* port1);

#endif
