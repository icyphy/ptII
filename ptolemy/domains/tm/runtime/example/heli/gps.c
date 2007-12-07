#include <stdio.h>
#include <time.h>

#include "circular_buffers.h"
#include "controller.h"
#include "gps.h"
#include "plant.h"
#include "return.h"
#include "socket.h"

int main(void)
{
        int socket;
        struct sockaddr_in address;
        struct timespec pause = {1, 0};
        gps_mesg_t gps_mesg;
        plant_outputs_t plant_outputs;
        int buffer_read_result;

        if (OK != reader_init_plant_outputs_buffer())
        {
                printf("In gps: error in call to ");
                printf("reader_init_plant_outputs_buffer\n");
                return -1;
        }

        if (OK != udp_client_init(&socket,
                                 &address,
                                 CONTROLLER_IP,
                                 CONTROLLER_GPS_PORT))
        {
                printf("In gps: error in call to ");
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
                        printf("In gps: error in call to ");
                        printf("read_plant_outputs_buffer\n");
                }

                if (OK == buffer_read_result)
                {
                        printf("gps send\n");

                        gps_mesg.this = plant_outputs.y6;
                        gps_mesg.that = plant_outputs.y7;

                        if (OK != udp_send(socket,
                                           &address,
                                           &gps_mesg,
                                           sizeof(gps_mesg_t)))
                        {
                                printf("In gps: error in call to ");
                                printf("udp_send\n");
                        }
                }

                if (0 != nanosleep(&pause, NULL))
                {
                        printf("In gps: error in call to ");
                        printf("nanosleep\n");
                        return -3;
                }
        }

        return 0;
}
