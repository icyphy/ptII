/***preinitBlock***/
#include "mbed.h"
#include "cc3000.h"
#include "TCPSocketConnection.h"
#include "TCPSocketServer.h"
#include "mbed.h"
#define SSID "CubeNet"//"Network1521"//
#define PASSWORD "modelbased"//"gbvonaue7b8ahpa7" //

#define ECHO_SERVER_ADDRESS     "192.168.1.33"
#define ECHO_SERVER_PORT        2000
#define MMA8451_I2C_ADDRESS     (0x1d<<1)

boolean sendingIn;
TCPSocketServer * server;
TCPSocketConnection * socket;
cc3000 * wifi;
int numR;
int packetSizeIn;
char *buf;
int *bufInt;
int dropCount;
int retval;
/**/

/***initBlock***/

//Init from the cc3000 example
//DigitalOut PWR_EN1(PTB2);
//DigitalOut PWR_EN2(PTB3);
numR = 0;
dropCount = 0;
retval = 0;

// Wi-Go set current to 500mA since we're turning on the Wi-Fi
/*PWR_EN1 = 0;
PWR_EN2 = 1;

NVIC_set_all_irq_priorities(0x3);
NVIC_SetPriority(SPI0_IRQn, 0x0);     // Wi-Fi SPI interrupt must be higher priority than SysTick
NVIC_SetPriority(PORTA_IRQn, 0x1);
NVIC_SetPriority(SysTick_IRQn, 0x2);  // SysTick set to lower priority than Wi-Fi SPI bus interrupt
PORTA->PCR[16] |= PORT_PCR_ISF_MASK;
PORTA->ISFR |= (1 << 16);*/

wifi = new cc3000(PTD4, PTC9, PTD0, SPI(PTD2, PTD3, PTD1), SSID, PASSWORD, WPA2, false);
//wifi = new cc3000(PTD4, PTC9, PTC4, SPI(PTC6, PTC7, PTC5), SSID, PASSWORD, WPA2, false);
wifi->init();
if (wifi->connect() == -1)
{
    printf("Failed to connect. Please verify connection details and try again. \r\n");
} else
{
    printf("IP address: %s \r\n", wifi->getIPAddress());
}
server = new TCPSocketServer;

server->bind(ECHO_SERVER_PORT);
server->listen();

socket = new TCPSocketConnection;
server->accept(*socket);
socket->set_blocking(false, 1500);

/**/

/***fireBlock***/
// The following is ordinary C code, except for
// the macro references to the input and output
// ports.
Token * packetToSendIn = $getNoPayload(packetToSend);
sendingIn = $getAndFree(sending);
packetSizeIn = $getAndFree(packetSize);

int array_size = packetToSendIn->payload.IntArray->size;
int iter;

char *packetString = new char[array_size];//malloc(array_size * sizeof(char));

buf = new char[packetSizeIn];

for (iter = 0; iter < array_size; iter++) {
    packetString[iter] = (char) IntArray_get(packetToSendIn, iter);
}

if (sendingIn == true) {
    socket->send_all((char *)packetString, array_size);
}
else {
    if (packetSizeIn > 0) {
        while (numR < packetSizeIn) {
            int retval = socket->receive(buf+numR, packetSizeIn);
            if(retval < 0)
            {
                dropCount++;
            } else
            {
                dropCount = 0;
                numR += retval;
            }
            if(dropCount > 1)
            {
                socket->close();
                delete socket;
                socket = new TCPSocketConnection;
                server->accept(*socket);
                socket->set_blocking(false, 1500);
                dropCount = 0;
            }
        }
        numR = 0;
    }
    else {
        while (retval < 0) {
            int retval = socket->receive(buf, packetSizeIn);
        }
    }
}

Token * bufInt = IntArray_new(0,0);

for (iter = 0; iter < packetSizeIn; iter++) {
    IntArray_insert(bufInt, (int) buf[iter]);
}

$put(dataOut, bufInt);

/**/

/***wrapupBlock***/
/**/
