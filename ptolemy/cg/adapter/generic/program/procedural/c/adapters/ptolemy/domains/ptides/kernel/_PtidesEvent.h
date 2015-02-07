/* The structure of a Ptides Event.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesEvent.h
 */

#ifndef PTIDESEVENT_H_
#define PTIDESEVENT_H_

#include "_ptTypes.h"
#include "_DEEvent.h"

#define PTIDESEVENT 11
#define IS_PTIDESEVENT(e) ((e)->typeEvent%100 == 11)

struct PtidesEvent {
    int typeEvent;

    struct Actor* _actor;
    int _depth;
    struct IOPort* _ioPort;
    int _microstep;
    int _priority;
    Time _timestamp;

    void (*free)(struct PtidesEvent*);

    struct Actor* (*actor)(struct PtidesEvent*);
    int (*compareTo)(struct PtidesEvent*, struct PtidesEvent*);
    int (*depth)(struct PtidesEvent*);
    bool (*equals)(struct PtidesEvent*, struct PtidesEvent*);
    long (*getVirtualBinNumber)(struct PtidesEvent*, double);
    bool (*hasTheSameTagAndDepthAs)(struct PtidesEvent*, struct DEEvent*);
    bool (*hasTheSameTagAs)(struct PtidesEvent*, struct PtidesEvent*);
    struct IOPort* (*ioPort)(struct PtidesEvent*);
    int (*microstep)(struct PtidesEvent*);
    Time (*timeStamp)(struct PtidesEvent*);
    void (*print)(struct PtidesEvent*);

    // new members
    int _channel;
    bool _isPureEvent;
    struct Receiver* _receiver;
    Time _absoluteDeadline;
    Token *_token;
    Time _sourceTimestamp;

    Time (*absoluteDeadline)(struct PtidesEvent*);
    int (*channel)(struct PtidesEvent*);
    bool (*isPureEvent)(struct PtidesEvent*);
    struct Receiver* (*receiver)(struct PtidesEvent*);
    Time (*sourceTimestamp)(struct PtidesEvent*);
    Token* (*token)(struct PtidesEvent*);
};

struct PtidesEvent* PtidesEvent_New();
void PtidesEvent_Init(struct PtidesEvent* event);
void PtidesEvent_New_Free(struct PtidesEvent* event);

void PtidesEvent_Print(struct PtidesEvent* event);

Time PtidesEvent_AbsoluteDeadline(struct PtidesEvent* event);
int PtidesEvent_Channel(struct PtidesEvent* event);
bool PtidesEvent_Equals(struct PtidesEvent* event, struct PtidesEvent* event2);
bool PtidesEvent_HasTheSameTagAs(struct PtidesEvent* event, struct PtidesEvent* event2);
bool PtidesEvent_IsPureEvent(struct PtidesEvent* event);
struct Receiver* PtidesEvent_Receiver(struct PtidesEvent* event);
Time PtidesEvent_SourceTimestamp(struct PtidesEvent* event);
Token* PtidesEvent_Token(struct PtidesEvent* event);

#endif
