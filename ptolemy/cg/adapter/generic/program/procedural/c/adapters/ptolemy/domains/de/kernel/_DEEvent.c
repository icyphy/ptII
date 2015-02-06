#include "_DEEvent.h"


struct DEEvent* DEEvent_New() {
    struct DEEvent * e = NULL;
    if ((e = calloc(1, sizeof(struct DEEvent))) == NULL) {
        fprintf(stderr, "Allocation Error (DEEvent_New)");
    }
    e->free = DEEvent_New_Free;
    DEEvent_Init(e);
    return e;
}
void DEEvent_Init(struct DEEvent* e) {
    e->typeEvent = DEEVENT;

    e->_depth = 0;
    e->_microstep = 0;
    e->_priority = 0;
    e->_timestamp = 0.0;

    e->_actor = NULL;
    e->_ioPort = NULL;

    e->actor = DEEvent_Actor;
    e->compareTo = DEEvent_CompareTo;
    e->depth = DEEvent_Depth;
    e->equals = DEEvent_Equals;
    e->getVirtualBinNumber = DEEvent_GetVirtualBinNumber;
    e->hasTheSameTagAndDepthAs = DEEvent_HasTheSameTagAndDepthAs;
    e->hasTheSameTagAs = DEEvent_HasTheSameTagAs;
    e->ioPort = DEEvent_IOPort;
    e->microstep = DEEvent_Microstep;
    e->timeStamp = DEEvent_TimeStamp;
}
void DEEvent_New_Free(struct DEEvent* event) {
    if (event)
        free(event);
}

struct Actor* DEEvent_Actor(struct DEEvent* event) {
    return event->_actor;
}
int DEEvent_CompareTo(struct DEEvent* event, struct DEEvent* event2) {
    if (event->_timestamp > event2->_timestamp) {
        return 1;
    } else if (event->_timestamp < event2->_timestamp) {
        return -1;
    } else if (event->_microstep > event2->_microstep) {
        return 1;
    } else if (event->_microstep < event2->_microstep) {
        return -1;
    } else if (event->_depth > event2->_depth) {
        return 1;
    } else if (event->_depth < event2->_depth) {
        return -1;
    } else if (event->_priority > event2->_priority) {
        return 1;
    } else if (event->_priority < event2->_priority) {
        return -1;
    } else {
        return 0;
    }
}
int DEEvent_Depth(struct DEEvent* event) {
    return event->_depth;
}
bool DEEvent_Equals(struct DEEvent* event, struct DEEvent* event2) {
    return (*(event->compareTo))(event, event2) == 0 && (*(event2->actor))(event2) == event->_actor;
}
long DEEvent_GetVirtualBinNumber(struct DEEvent* event, double binWidth) {
    return (long) ((*(event->timeStamp))(event) / binWidth);
}
bool DEEvent_HasTheSameTagAndDepthAs(struct DEEvent* event, struct DEEvent* event2) {
    return (*(event->hasTheSameTagAs))(event, event2) && event->_depth == (*(event2->depth))(event2);
}
bool DEEvent_HasTheSameTagAs(struct DEEvent* event, struct DEEvent* event2) {
    return event->_timestamp == (*(event2->timeStamp))(event2)
           && event->_microstep == (*(event2->microstep))(event2);
}
struct IOPort* DEEvent_IOPort(struct DEEvent* event) {
    return event->_ioPort;
}
int DEEvent_Microstep(struct DEEvent* event) {
    return event->_microstep;
}
Time DEEvent_TimeStamp(struct DEEvent* event) {
    return event->_timestamp;
}
