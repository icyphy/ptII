#include <stdio.h>
#include <sys/poll.h>

#include "circular_buffers.h"
#include "controller.h"
#include "ins.h"
#include "gps.h"
#include "return.h"
#include "socket.h"

int main(void)
{
        int ins_socket;
        int gps_socket;
        struct pollfd await[2];
        ins_mesg_t ins_mesg;
        gps_mesg_t gps_mesg;

        if (OK != writer_init_gps_buffer())
        {
                printf("In controller_server: error in call to ");
                printf("writer_init_gps_buffer\n");
        }

        if (OK != writer_init_ins_buffer())
        {
                printf("In controller_server: error in call to ");
                printf("writer_init_ins_buffer\n");
        }

        if (OK != udp_server_init(&ins_socket, CONTROLLER_INS_PORT))
        {
                printf("In controller_server: error in call to ");
                printf("udp_server_init for ins\n");
                return -1;
        }

        if (OK != udp_server_init(&gps_socket, CONTROLLER_GPS_PORT))
        {
                printf("In controller_server: error in call to ");
                printf("udp_server_init for gps\n");
                return -2;
        }

        await[0].fd = ins_socket;
        await[0].events = POLLIN | POLLPRI;
        await[1].fd = gps_socket;
        await[1].events = POLLIN | POLLPRI;

        while (1)
        {
                if (-1 == poll(await, 2, -1))
                {
                        printf("In controller_server: error in call to ");
                        printf("poll\n");
                }

                if (await[0].revents & (POLLIN | POLLPRI))
                {
                        printf("ins receive ");

                        if (OK != udp_receive(ins_socket,
                                              &ins_mesg,
                                              sizeof(ins_mesg_t)))
                        {
                                printf("In controller_server: ");
                                printf("error in call to ");
                                printf("udp_receive for ins\n");
                        }

                        if (OK != write_ins_buffer(&ins_mesg))
                        {
                                printf("In controller_server: ");
                                printf("error in call to ");
                                printf("write_ins_buffer\n");
                        }
                }

                if (await[1].revents & (POLLIN | POLLPRI))
                {
                        printf("gps receive");

                        if (OK != udp_receive(gps_socket,
                                              &gps_mesg,
                                              sizeof(gps_mesg_t)))
                        {
                                printf("In controller_server: ");
                                printf("error in call to ");
                                printf("udp_receive for gps\n");
                        }

                        if (OK != write_gps_buffer(&gps_mesg))
                        {
                                printf("In controller_server: ");
                                printf("error in call to ");
                                printf("write_gps_buffer\n");
                        }
                }

                printf("\n");
        }

        return 0;
}
