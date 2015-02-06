#include "_PtidesEvent.h"


struct PtidesEvent* PtidesEvent_New() {
    struct PtidesEvent * e = NULL;
    if ((e = calloc(1, sizeof(struct PtidesEvent))) == NULL) {
        fprintf(stderr, "Allocation Error (PtidesEvent_New)");
    }
    e->free = PtidesEvent_New_Free;
    PtidesEvent_Init(e);
    return e;
}
void PtidesEvent_Init(struct PtidesEvent* e) {
    DEEvent_Init((struct DEEvent*) e);

    e->typeEvent = PTIDESEVENT;

    e->absoluteDeadline = PtidesEvent_AbsoluteDeadline;
    e->channel = PtidesEvent_Channel;
    e->equals = PtidesEvent_Equals;
    e->hasTheSameTagAs = PtidesEvent_HasTheSameTagAs;
    e->isPureEvent = PtidesEvent_IsPureEvent;
    e->receiver = PtidesEvent_Receiver;
    e->sourceTimestamp = PtidesEvent_SourceTimestamp;
    e->token = PtidesEvent_Token;
}
void PtidesEvent_New_Free(struct PtidesEvent* event) {
    DEEvent_New_Free((struct DEEvent*) event);
}

Time PtidesEvent_AbsoluteDeadline(struct PtidesEvent* event) {
    return event->_absoluteDeadline;
}
int PtidesEvent_Channel(struct PtidesEvent* event) {
    return event->_channel;
}
bool PtidesEvent_Equals(struct PtidesEvent* event, struct PtidesEvent* event2) {
    bool result = DEEvent_Equals((struct DEEvent*) event, (struct DEEvent*) event2);
    if (!event->isPureEvent(event2)) {
        Token* token1 = event2->token(event2);
        Token* token2 = event->_token;
        result = result && !memcmp(token1, token2, sizeof(Token));
    } else {
        if (event2->absoluteDeadline(event2) == -DBL_MAX
                || event->_absoluteDeadline == -DBL_MAX) {
            return false;
        } else {
            result = result
                     && event2->absoluteDeadline(event2) == event->_absoluteDeadline;
        }
    }
    return result && event2->isPureEvent(event2) == event->_isPureEvent
           && event2->receiver(event2) == event->_receiver
           && event2->channel(event2) == event->_channel;
}
bool PtidesEvent_HasTheSameTagAs(struct PtidesEvent* event, struct PtidesEvent* event2) {
    struct Actor* actor = event2->actor(event2);
    if (actor == NULL) {
        actor = (struct Actor*) event2->ioPort(event2)->container;
    }
    double clockSyncBound = -DBL_MAX;
    clockSyncBound = actor->_clockSynchronizationBound;
    if (clockSyncBound == -DBL_MAX) {
        clockSyncBound = 0.0;
    }

    return (event->_timestamp == event2->timeStamp(event2)
            && event->_microstep == event2->microstep(event2))
           || event->_timestamp <= event2->timeStamp(event2)
           || (event->_timestamp - clockSyncBound <= event2->timeStamp(event2)
               && event->_timestamp + clockSyncBound >= event2->timeStamp(event2));
}
bool PtidesEvent_IsPureEvent(struct PtidesEvent* event) {
    return event->_isPureEvent;
}
struct Receiver* PtidesEvent_Receiver(struct PtidesEvent* event) {
    return event->_receiver;
}
Time PtidesEvent_SourceTimestamp(struct PtidesEvent* event) {
    return event->_sourceTimestamp;
}
Token* PtidesEvent_Token(struct PtidesEvent* event) {
    return event->_token;
}
