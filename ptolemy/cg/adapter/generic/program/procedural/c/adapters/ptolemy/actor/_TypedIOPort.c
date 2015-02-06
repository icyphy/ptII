#include "_TypedIOPort.h"

struct TypedIOPort* TypedIOPort_New() {
    struct TypedIOPort* newTypedIOPort = calloc(1, sizeof(struct TypedIOPort));
    if (newTypedIOPort == NULL) {
        fprintf(stderr, "Allocation error : TypedIOPort_New\n");
        exit(-1);
    }
    TypedIOPort_Init(newTypedIOPort);
    newTypedIOPort->free = TypedIOPort_New_Free;

    return newTypedIOPort;
}
void TypedIOPort_Init(struct TypedIOPort* port) {
    IOPort_Init((struct IOPort*)port);

    port->typePort = TYPEDIOPORT;

    port->_type = -1;
    port->getType = TypedIOPort_GetType;
}
void TypedIOPort_New_Free(struct TypedIOPort* port) {
    IOPort_New_Free((struct IOPort*)port);
}

char TypedIOPort_GetType(struct TypedIOPort* port) {
    return port->_type;
}
