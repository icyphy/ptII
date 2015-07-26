/***preinitBlock***/
#include "mbed.h"
#define bufferSize 6
int sendBuf[bufferSize];
boolean sending_val;
int state;
int numberOfPackets;

/**/

/***initBlock***/
state = 0;
numberOfPackets = 40;
/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.

if (state == 0) {
    //Init packet
    sendBuf[0] = 0x24;
    sendBuf[1] = 0x0A;
    sendBuf[2] = 0x03;
    sendBuf[3] = 0x00;
    sendBuf[4] = (sendBuf[0]+sendBuf[1]+sendBuf[2]+sendBuf[3])%256;
    sendBuf[5] = '#';

    sending_val = true;

    state = 1;
}
else if (state == 1) {
    //Packet for data request
    sendBuf[0] = 0x24;
    sendBuf[1] = 0x0A;
    sendBuf[2] = 0x03;
    sendBuf[3] = 0x01;
    sendBuf[4] = (sendBuf[0]+sendBuf[1]+sendBuf[2]+sendBuf[3])%256;
    sendBuf[5] = '#';

    sending_val = true;

    state = 2;
}
else {
    //Empty packet while getting data
    sendBuf[0] = 0;
    sendBuf[1] = 0;
    sendBuf[2] = 0;
    sendBuf[3] = 0;
    sendBuf[4] = 0;
    sendBuf[5] = 0;

    sending_val = false;
}

Token * sendArray = IntArray_new(0,0);

int i;
for (i = 0; i < bufferSize; i++) {
IntArray_insert(sendArray, sendBuf[i]);
}

$put(packetsOut, sendArray);
$put(sendStatus, sending_val);
$put(totalPackets, numberOfPackets);

/**/

/***wrapupBlock***/
/**/
