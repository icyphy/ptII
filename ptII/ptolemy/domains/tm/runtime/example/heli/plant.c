#include <stdio.h>
#include <time.h>

#include "circular_buffers.h"
#include "derivatives.h"
#include "plant.h"
#include "return.h"
#include "rk4.h"

#define PLANT_STATE_SIZE (22) /* 18 states + 4 control inputs */
#define TIME_STEP_NANOSECONDS (100000000)
#define TIME_STEP_SECONDS ((float) (((float) TIME_STEP_NANOSECONDS)/((float) 1000000000)))

int plant(plant_inputs_t *inputs, plant_outputs_t *outputs)
{
        /* extra field needed by numerical recipes routines: */
        static float plant_state[PLANT_STATE_SIZE+1] =
        {
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0
        };
        float derivative_plant_state[PLANT_STATE_SIZE+1];

        static int iteration = 0;
        float time = iteration * TIME_STEP_SECONDS;

        /* the plant state includes the four controller outputs,
           because these are used in the calculation of derivatives: */

        plant_state[19] = inputs->y19;
        plant_state[20] = inputs->y20;
        plant_state[21] = inputs->y21;
        plant_state[22] = inputs->y22;

        derivatives(time,
                    plant_state,
                    derivative_plant_state);
        rk4(plant_state,
            derivative_plant_state,
            PLANT_STATE_SIZE,
            time,
            TIME_STEP_SECONDS,
            plant_state,
            derivatives);

        outputs->y1 = plant_state[1];
        outputs->y2 = plant_state[2];
        outputs->y3 = plant_state[3];
        outputs->y4 = plant_state[4];
        outputs->y5 = plant_state[5];
        outputs->y6 = plant_state[6];
        outputs->y7 = plant_state[7];
        outputs->y8 = plant_state[8];
        outputs->y9 = plant_state[9];
        outputs->y10 = plant_state[10];
        outputs->y11 = plant_state[11];

        printf("%.3f %.3f %.3f %.3f %.3f %.3f\n",
               plant_state[6],
               plant_state[7],
               plant_state[8],
               plant_state[9],
               plant_state[10],
               plant_state[11]);

        iteration++;

        return OK;
}

int main(void)
{
        plant_inputs_t inputs;
        plant_outputs_t outputs;
        int buffer_read_result;
        int buffer_write_result;
        struct timespec pause = {0, TIME_STEP_NANOSECONDS};

        if (OK != writer_init_plant_outputs_buffer())
        {
                printf("In plant: error in call to ");
                printf("writer_init_plant_outputs_buffer\n");
                return -1;
        }

        if (OK != reader_init_plant_inputs_buffer())
        {
                printf("In plant: error in call to ");
                printf("reader_init_plant_inputs_buffer\n");
                return -1;
        }

        while (1)
        {
                buffer_read_result =
                        read_plant_inputs_buffer(&inputs);

                if (OK != buffer_read_result &&
                    NO_DATA != buffer_read_result)
                {
                        printf("In plant: error in call to ");
                        printf("read_plant_inputs_buffer\n");
                }

                if (NO_DATA == buffer_read_result)
                {
                        inputs.y19 = 0.0;
                        inputs.y20 = 0.0;
                        inputs.y21 = 0.0;
                        inputs.y22 = 0.0;
                }

                printf("%.3f %.3f %.3f %.3f\n",
                       inputs.y19,
                       inputs.y20,
                       inputs.y21,
                       inputs.y22);

                if (OK != plant(&inputs, &outputs))
                {
                        printf("In plant: error in call to ");
                        printf("plant\n");
                }

                buffer_write_result =
                        write_plant_outputs_buffer(&outputs);

                if (OK != buffer_read_result &&
                    NO_DATA != buffer_read_result)
                {
                        printf("In plant: error in call to ");
                        printf("write_plant_outputs_buffer\n");
                }

                if (0 != nanosleep(&pause, NULL))
                {
                        printf("In plant: error in call to ");
                        printf("nanosleep\n");
                        return -3;
                }
        }
}
