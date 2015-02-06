#include "_PtidesMirrorPort.h"

struct PtidesMirrorPort* PtidesMirrorPort_New() {
    struct PtidesMirrorPort* newPtidesPort = calloc(1, sizeof(struct PtidesMirrorPort));
    if (newPtidesPort == NULL) {
        fprintf(stderr, "Allocation error : PtidesMirrorPort_New\n");
        exit(-1);
    }
    PtidesMirrorPort_Init(newPtidesPort);
    newPtidesPort->free = PtidesMirrorPort_New_Free;

    return newPtidesPort;
}
void PtidesMirrorPort_Init(struct PtidesMirrorPort* port) {
    TypedIOPort_Init((struct TypedIOPort*)port);

    port->typePort = PTIDESMIRRORPORT;

    port->setAssociatedPort = PtidesMirrorPort_SetAssociatedPort;
}
void PtidesMirrorPort_New_Free(struct PtidesMirrorPort* port) {
    TypedIOPort_New_Free((struct TypedIOPort*)port);
}

void PtidesMirrorPort_SetAssociatedPort(struct PtidesMirrorPort* port, struct PtidesMirrorPort* port1) {
    port->_associatedPort = (struct PtidesPort*)port1;
    port1->_associatedPort = (struct PtidesPort*)port;
}
