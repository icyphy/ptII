#include <stdio.h>
#include <time.h>

#include "circular_buffers.h"
#include "controller.h"
#include "ins.h"
#include "plant.h"
#include "return.h"
#include "socket.h"

int main(void)
{
        int socket;
        struct sockaddr_in address;
        struct timespec pause = {0, 100000000};
        ins_mesg_t ins_mesg;
        plant_outputs_t plant_outputs;
        int buffer_read_result;

        if (OK != reader_init_plant_outputs_buffer())
        {
                printf("In ins: error in call to ");
                printf("reader_init_plant_outputs_buffer\n");
                return -1;
        }

        if (OK != udp_client_init(&socket,
                                 &address,
                                 CONTROLLER_IP,
                                 CONTROLLER_INS_PORT))
        {
                printf("In ins: error in call to ");
                printf("udp_client_init\n");
                return -2;
        }

        while (1)
        {
                buffer_read_result =
                        read_plant_outputs_buffer(&plant_outputs);

                if (OK != buffer_read_result &&
                    NO_DATA != buffer_read_result)
                {
                        printf("In ins: error in call to ");
                        printf("read_plant_outputs_buffer\n");
                }

                if (OK == buffer_read_result)
                {
                        printf("ins send\n");

                        ins_mesg.y6 = plant_outputs.y6;
                        ins_mesg.y7 = plant_outputs.y7;
                        ins_mesg.y8 = plant_outputs.y8;
                        ins_mesg.y9 = plant_outputs.y9;
                        ins_mesg.y10 = plant_outputs.y10;
                        ins_mesg.y11 = plant_outputs.y11;

                        if (OK != udp_send(socket,
                                           &address,
                                           &ins_mesg,
                                           sizeof(ins_mesg_t)))
                        {
                                printf("In ins: error in call to ");
                                printf("udp_send\n");
                        }
                }

                if (0 != nanosleep(&pause, NULL))
                {
                        printf("In ins: error in call to ");
                        printf("nanosleep\n");
                        return -3;
                }
        }
}
