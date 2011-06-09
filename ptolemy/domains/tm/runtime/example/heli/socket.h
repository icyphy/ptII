#ifndef SOCKETS_H
#define SOCKETS_H

#include <arpa/inet.h>

int udp_client_init(int *sock,
                    struct sockaddr_in *address,
                    char *ip,
                    unsigned port);

int udp_server_init(int *sock,
                    unsigned port);

int udp_send(int sock,
             struct sockaddr_in *address,
             void *data,
             unsigned size);

int udp_receive(int sock,
                void *data,
                unsigned size);

#endif /* #ifndef SOCKETS_H */

