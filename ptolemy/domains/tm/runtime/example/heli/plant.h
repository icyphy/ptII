#ifndef PLANT_H
#define PLANT_H

#define PLANT_IP "127.0.0.1"
#define PLANT_ACTUATORS_PORT 49154

/* -------------------------------------------------------------------
 *
 * Type: plant_outputs_t
 * Type: plant_inputs_t
 *
 * ---------------------------------------------------------------- */

#ifdef PLANT
typedef struct
{
        float y1; /* fixme */
        float y2;
        float y3;
        float y4;
        float y5;
        float y6;
        float y7;
        float y8;
        float y9;
        float y10;
        float y11;
}
plant_outputs_t;

typedef struct
{
        float y19; /* fixme */
        float y20;
        float y21;
        float y22;
}
plant_inputs_t;
#endif

#endif /* #ifndef PLANT_H */
