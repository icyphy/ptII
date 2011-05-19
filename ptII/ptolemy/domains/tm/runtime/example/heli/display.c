#include <stdio.h>
#include <time.h>

#include "circular_buffers.h"
#include "display.h"
#include "ins.h"
#include "plant.h"
#include "return.h"
#include "socket.h"

int main(void)
{
        int socket;
        struct sockaddr_in address;
        struct timespec pause = {0, 100000000};
        display_mesg_t display_mesg;
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
                                 DISPLAY_IP,
                                 DISPLAY_PORT))
        {
                printf("In display: error in call to ");
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
                        printf("In display: error in call to ");
                        printf("read_plant_outputs_buffer\n");
                }

                if (OK == buffer_read_result)
                {
                        display_mesg.y1 = plant_outputs.y1;
                        display_mesg.y2 = plant_outputs.y2;
                        display_mesg.y3 = plant_outputs.y3;
                        display_mesg.y4 = plant_outputs.y4;
                        display_mesg.y5 = plant_outputs.y5;
                        display_mesg.y6 = plant_outputs.y6;

                        if (OK != udp_send(socket,
                                           &address,
                                           &display_mesg,
                                           sizeof(display_mesg_t)))
                        {
                                printf("In display: error in call to ");
                                printf("udp_send\n");
                        }

                        printf("%.3f %.3f %.3f %.3f %.3f %.3f\n",
                               display_mesg.y1,
                               display_mesg.y2,
                               display_mesg.y3,
                               display_mesg.y4,
                               display_mesg.y5,
                               display_mesg.y6);
                }

                if (0 != nanosleep(&pause, NULL))
                {
                        printf("In display: error in call to ");
                        printf("nanosleep\n");
                        return -3;
                }
        }
}
