#include <arpa/inet.h>
#include <errno.h>
#include <sys/socket.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "return.h"
#include "socket.h"

/* -------------------------------------------------------------------
 *
 * Function udp_client_init: initialize a client UDP socket. the
 * client will send data.
 *
 * this function zeros the address, sets sock equal to a socket
 * of type SOCK_DGRAM, and initializes the address to the input
 * ip and port numbers. now the ip and port will not be necessary
 * when sending data, only the address must be passed.  client must
 * save socket and address in order to call udp_send.
 *
 * Arguments: sock, a pointer to an uninitialized socket; address,
 * initialized for future send socket calls; ip, the IP address of the
 * server you want to send to; port, the port of the server to send to.
 *
 * ---------------------------------------------------------------- */

int udp_client_init(int *sock,
                    struct sockaddr_in *address,
                    char *ip,
                    unsigned port)
{
        bzero((char *) address, sizeof(struct sockaddr_in));
        if (-1 == (*sock=socket(PF_INET, SOCK_DGRAM, 0)))
        {
                printf("In udp_client_init: ");
                printf("error in call to socket\n");
                return ERROR;
        }
        address->sin_family = PF_INET;
        address->sin_port = htons(port);
        if (-1 == (address->sin_addr.s_addr = inet_addr(ip)))
        {
                printf("In udp_client_init: ");
                printf("invalid host %s\n", ip);
                close(*sock);
                return ERROR;
        }
        return OK;
}

/* -------------------------------------------------------------------
 *
 * Function udp_server_init: initialize a server UDP socket. the
 * server will receive data.
 *
 * sets sock equal to SOCK_DGRAM, zeros address, sets address to
 * receive from any address (client) and its own port number.
 *
 * Arguments: sock, a pointer to an uninitialized socket; port, the
 * port of the server.  in order to recieve the port must be known
 * by a client who will send to it.
 *
 * ---------------------------------------------------------------- */

int udp_server_init(int *sock,
                    unsigned port)
{
        struct sockaddr_in address;
        *sock = socket(AF_INET, SOCK_DGRAM, 0);
        if (-1 == *sock)
        {
                printf("In udp_server_init: ");
                printf("error in call to socket\n");
                return ERROR;
        }
        else
        {
                bzero((char *) &address, sizeof(address));
                address.sin_family = AF_INET;
                address.sin_addr.s_addr = htonl(INADDR_ANY);
                address.sin_port = htons(port);
                if (-1 == bind(*sock, (struct sockaddr *) &address,
                               sizeof(address)))
                {
                        printf("In udp_server_init: ");
                        printf("error in call to bind, ");
                        printf("errno = %d\n", errno);
                        return ERROR;
                }
        }
        return OK;
}

/* -------------------------------------------------------------------
 *
 * Function udp_send: send data over a UDP socket. called by client
 *
 * Arguments: sock, a pointer to an initialized UDP socket; address,
 * the address to send to given by udp_client_init; data, the
 * data to send; size, the number of bytes of the data to send.
 *
 * ---------------------------------------------------------------- */

int udp_send(int sock,
             struct sockaddr_in *address,
             void *data,
             unsigned size)
{
        if (-1 == sendto(sock,
                         (caddr_t) data,
                         size,
                         0,
                         (struct sockaddr *) address,
                         sizeof(struct sockaddr_in)))
        {
                printf("In udp_send: error in call to sendto\n");
                return ERROR;
        }
        return OK;
}

/* -------------------------------------------------------------------
 *
 * Function udp_receive: receive data over a UDP socket. called by
 * server. the local sockaddr_in address is set to the address of the
 * sending client by recvfrom.  this address is not returned.
 *
 * Arguments: sock, a pointer to an initialized UDP socket; data, a
 * buffer into which the received data gets put; size, the maximum
 * number of bytes to receive.
 *
 * ---------------------------------------------------------------- */

int udp_receive(int sock,
                void *data,
                unsigned size)
{
        struct sockaddr_in address;
        int len = sizeof(address);
        if (-1 == recvfrom(sock,
                           data,
                           size,
                           0,
                           (struct sockaddr *) &address,
                           &len))
        {
                printf("In udp_receive: error in call to recvfrom\n");
                return ERROR;
        }
        return OK;
}
