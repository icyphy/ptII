#include <stdio.h>
#include <sys/poll.h>

#include "actuators.h"
#include "circular_buffers.h"
#include "plant.h"
#include "return.h"
#include "socket.h"

int main(void)
{
        int actuators_socket;
        actuators_mesg_t actuators_mesg;
        plant_inputs_t plant_inputs;

        if (OK != writer_init_plant_inputs_buffer())
        {
                printf("In plant_server: error in call to ");
                printf("writer_init_plant_inputs_buffer\n");
        }

        if (OK != udp_server_init(&actuators_socket, PLANT_ACTUATORS_PORT))
        {
                printf("In plant_server: error in call to ");
                printf("udp_server_init\n");
                return -1;
        }

        while (1)
        {
                if (OK != udp_receive(actuators_socket,
                                      &actuators_mesg,
                                      sizeof(actuators_mesg_t)))
                {
                        printf("In plant_server: ");
                        printf("error in call to ");
                        printf("udp_receive\n");
                }

                printf("%.3f %.3f %.3f %.3f\n",
                       actuators_mesg.y19,
                       actuators_mesg.y20,
                       actuators_mesg.y21,
                       actuators_mesg.y22);

                plant_inputs.y19 = actuators_mesg.y19;
                plant_inputs.y20 = actuators_mesg.y20;
                plant_inputs.y21 = actuators_mesg.y21;
                plant_inputs.y22 = actuators_mesg.y22;

                if (OK != write_plant_inputs_buffer(&plant_inputs))
                {
                        printf("In plant_server: ");
                        printf("error in call to ");
                        printf("write_plant_inputs_buffer\n");
                }
        }

        return 0;
}
