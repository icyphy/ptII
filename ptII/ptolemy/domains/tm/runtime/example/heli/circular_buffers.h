#ifndef CIRCULAR_BUFFERS_H
#define CIRCULAR_BUFFERS_H

#ifdef CONTROLLER
#include "gps.h"
#include "ins.h"
#endif

#ifdef PLANT
#include "plant.h"
#endif

#ifdef CONTROLLER
int reader_init_gps_buffer(void);
int reader_init_ins_buffer(void);
#endif

#ifdef PLANT
int reader_init_plant_outputs_buffer(void);
int reader_init_plant_inputs_buffer(void);
#endif

#ifdef CONTROLLER
int writer_init_gps_buffer(void);
int writer_init_ins_buffer(void);
#endif

#ifdef PLANT
int writer_init_plant_outputs_buffer(void);
int writer_init_plant_inputs_buffer(void);
#endif

#ifdef CONTROLLER
int write_gps_buffer(gps_mesg_t *data);
int write_ins_buffer(ins_mesg_t *data);
#endif

#ifdef PLANT
int write_plant_outputs_buffer(plant_outputs_t *data);
int write_plant_inputs_buffer(plant_inputs_t *data);
#endif

#ifdef CONTROLLER
int read_gps_buffer(gps_mesg_t *data);
int read_ins_buffer(ins_mesg_t *data);
#endif

#ifdef PLANT
int read_plant_outputs_buffer(plant_outputs_t *data);
int read_plant_inputs_buffer(plant_inputs_t *data);
#endif

#endif /* #ifndef CIRCULAR_BUFFERS_H */
