#include <stdio.h>
#include <string.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/types.h>

#include "circular_buffers.h"
#include "gps.h"
#include "ins.h"
#include "return.h"

#ifdef PLANT
#include "plant.h"
#endif

/* -------------------------------------------------------------------
 *
 * Constant: CONTROLLER
 * Constant: PLANT
 *
 * These constants indicate whether this file is being compiled as
 * part of the controller, or as part of the plant.  Exactly one of
 * these constants must be defined.
 *
 * ---------------------------------------------------------------- */

#if (!defined(CONTROLLER) && !defined(PLANT))
#error Neither CONTROLLER nor PLANT is defined.
#endif

#if (defined(CONTROLLER) && defined(PLANT))
#error Both CONTROLLER and PLANT are defined.
#endif

/* -------------------------------------------------------------------
 *
 * Constant: GPS_MESG_ID
 * Constant: INS_MESG_ID
 * Constant: PLANT_OUTPUTS_ID
 * Constant: PLANT_INPUTS_ID
 *
 * These constants serve as identifiers for different types of shared
 * memory.  They are necessary for using shared memory in the Linux
 * implementation; in the VxWorks implementation, they should not be
 * necessary.
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
#define GPS_MESG_ID      ('a') /* shared memory for the GPS messages */
#define INS_MESG_ID      ('b') /* shared memory for the INS messages */
#endif

#ifdef PLANT
#define PLANT_OUTPUTS_ID ('c') /* shared memory for the plant outputs */
#define PLANT_INPUTS_ID  ('d') /* shared memory for the plant inputs */
#endif

/* -------------------------------------------------------------------
 *
 * Constant: NUM_GPS_ENTRIES
 * Constant: NUM_INS_ENTRIES
 * Constant: NUM_PLANT_OUTPUTS_ENTRIES
 * Constant: NUM_PLANT_INPUTS_ENTRIES
 *
 * These constants determine the number of entries in the circular
 * buffers.
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
#define NUM_GPS_ENTRIES           (8) /* number of entries in gps
                                         circular buffer */
#define NUM_INS_ENTRIES           (8) /* number of entries in ins
                                         circular buffer */
#endif
#ifdef PLANT
#define NUM_PLANT_OUTPUTS_ENTRIES (8) /* number of entries in plant
                                         outputs circular buffer */
#define NUM_PLANT_INPUTS_ENTRIES  (8) /* number of entries in plant
                                         inputs circular buffer */
#endif

/* -------------------------------------------------------------------
 *
 * Type: circular_buffer_state_t
 *
 * This type contains state information for a circular buffer.  If
 * has_valid_entry is 0, the circular buffer contains no valid
 * entries.  If has_valid_entry is 1, then the entries between begin
 * and end are valid.  For example, suppose the buffer contains 4
 * elements, has_valid_entry is 1, begin is 3, and end is 1.  Then
 * entries 3, 4, 0, and 1 of the circular buffer are valid.
 *
 *
 * Notes: Circular buffers are implemented as regions of shared
 * memory.  Their first sizeof(circular_buffer_state_t) bytes contain
 * the circular buffer state.  The rest of the circular buffer
 * contains circular buffer entries.  For a given circular buffer,
 * each of these slots has the same size, and must contain the same
 * type of data -- either gps_mesg_t, ins_mesg_t, plant_outputs_t, or
 * plant_inputs_t.
 *
 * ---------------------------------------------------------------- */

typedef struct
{
        unsigned has_valid_entry; /* 1 if some entry of the buffer is
                                     is valid; 0 otherwise */
        unsigned begin; /* first entry of the buffer that's valid */
        unsigned end; /* last entry of the buffer that's valid */
}
circular_buffer_state_t;

/* -------------------------------------------------------------------
 *
 * Function: valid_id
 *
 *
 * Arguments:
 *
 * char id: an identifier for shared memory.
 *
 *
 * Returns:
 *
 * If CONTROLLER is defined: 0 if id is either GPS_MESG_ID or
 * INS_MESG_ID; -1 otherwise.
 *
 * If PLANT is defined: 0 if id is either PLANT_OUTPUTS_ID or
 * PLANT_INPUTS_ID; -1 otherwise.
 *
 * ---------------------------------------------------------------- */

static int valid_id(char id)
{
#ifdef CONTROLLER
        if (GPS_MESG_ID != id && INS_MESG_ID != id)
#endif
#ifdef PLANT
        if (PLANT_OUTPUTS_ID != id && PLANT_INPUTS_ID != id)
#endif
        {
                printf("In valid_id: error: ");
                printf("id is invalid");
                return -1;
        }

        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: get_entry_size
 *
 *
 * Arguments:
 *
 * char id: the type of the contents of the entries of the circular
 * buffer
 *
 * unsigned *entry_size: upon success, *entry_size will be set to the
 * size of the entry type.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * ---------------------------------------------------------------- */

static int get_entry_size(char id,
                          unsigned *entry_size)
{
        if (0 != valid_id(id))
        {
                return -1;
        }

        switch (id)
        {
#ifdef CONTROLLER
        case GPS_MESG_ID:
                *entry_size = sizeof(gps_mesg_t);
                break;
        case INS_MESG_ID:
                *entry_size = sizeof(ins_mesg_t);
                break;
#endif
#ifdef PLANT
        case PLANT_OUTPUTS_ID:
                *entry_size = sizeof(plant_outputs_t);
                break;
        case PLANT_INPUTS_ID:
                *entry_size = sizeof(plant_inputs_t);
                break;
#endif
        }
        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: get_num_entries
 *
 *
 * Arguments:
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 * unsigned *num_entries: upon success, *num_entries will be set to
 * number of entries of the circular buffer.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * ---------------------------------------------------------------- */

static int get_num_entries(char id,
                           unsigned *num_entries)
{
        if (0 != valid_id(id))
        {
                return -1;
        }

        switch (id)
        {
#ifdef CONTROLLER
        case GPS_MESG_ID:
                *num_entries = NUM_GPS_ENTRIES;
                break;
        case INS_MESG_ID:
                *num_entries = NUM_INS_ENTRIES;
                break;
#endif
#ifdef PLANT
        case PLANT_OUTPUTS_ID:
                *num_entries = NUM_PLANT_OUTPUTS_ENTRIES;
                break;
        case PLANT_INPUTS_ID:
                *num_entries = NUM_PLANT_INPUTS_ENTRIES;
                break;
#endif
        }
        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: get_buffer_size
 *
 *
 * Arguments:
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 * unsigned *buffer_size: upon success, *buffer_size will be set to
 * the size, in bytes, of the circular buffer buffer.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * ---------------------------------------------------------------- */

static int get_buffer_size(char id,
                           unsigned *buffer_size)
{
        unsigned entry_size;
        unsigned num_entries;

        if (0 != valid_id(id))
        {
                return -1;
        }

        if (0 != get_entry_size(id, &entry_size))
        {
                return -2;
        }

        if (0 != get_num_entries(id, &num_entries))
        {
                return -3;
        }

        *buffer_size = sizeof(circular_buffer_state_t) +
                num_entries * entry_size;

        return 0;
}

/* -------------------------------------------------------------------
 *
 * Constant: SHARED_MEM_DIRECTORY
 *
 * This constant determines the path name for the shared memory.
 *
 * ---------------------------------------------------------------- */

#define SHARED_MEM_DIRECTORY ("/tmp")

/* -------------------------------------------------------------------
 *
 * Function: get_circular_buffer
 *
 *
 * Arguments:
 *
 * void **circular_buffer: upon successful exit, *circular_buffer will
 * point to a circular buffer.
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * -2 if the call to get_buffer_size fails
 *
 * -3 if shared memory cannot be opened
 *
 * -4 if the shared memory cannot be attached
 *
 *
 * Notes: may be called by both the writer and the readers of the
 * circular buffer.
 *
 * ---------------------------------------------------------------- */

static int get_circular_buffer(void **circular_buffer,
                               char id)
{
        unsigned buffer_size;
        key_t key;
        int shmid;
        unsigned created_mem = 0;

        if (0 != valid_id(id))
        {
                return -1;
        }

        if (0 != get_buffer_size(id, &buffer_size))
        {
                printf("In get_circular_buffer: ");
                printf("error in call to get_buffer_size\n");
                return -2;
        }

        /* create unique key via call to ftok */
        key = ftok(SHARED_MEM_DIRECTORY, id);

        /* open the shared memory segment -- create if necessary */
        shmid = shmget(key, buffer_size, IPC_CREAT|IPC_EXCL|0666);
        if (-1 == shmid)
        {
                shmid = shmget(key, buffer_size, 0);
                /* segment probably already exists -- try as a client */
                if (-1 == shmid)
                {
                        perror("In get_circular_buffer");
                        return -3;
                }
        }
        else
        {
                created_mem = 1;
        }

        /* attach (map) the shared memory segment into the current
           process */
        *circular_buffer = shmat(shmid, 0, 0);

        if (-1 == (int) *circular_buffer)
        {
                perror("In get_circular_buffer");
                return -4;
        }

        if (1 == created_mem)
        {
                bzero(*circular_buffer, buffer_size);
                ((circular_buffer_state_t *)
                 *circular_buffer)->has_valid_entry = 0;
        }

        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: init_circular_buffer
 *
 *
 * Arguments:
 *
 * void **circular_buffer: upon successful exit, *circular_buffer will
 * point to a circular buffer.  All bytes of the entries of this
 * buffer will be set to zero, and the has_valid_entry field of the
 * state of the buffer will be set to 0.
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * -2 if the call to get_buffer_size fails
 *
 * -3 if the call to get_circular_buffer fails
 *
 *
 * Notes: should be called only by the writer of the circular_buffer.
 *
 * ---------------------------------------------------------------- */

static int init_circular_buffer(void **circular_buffer,
                                char id)
{
        unsigned buffer_size;

        if (0 != valid_id(id))
        {
                return -1;
        }

        if (0 != get_buffer_size(id, &buffer_size))
        {
                return -2;
        }

        if (0 != get_circular_buffer(circular_buffer, id))
        {
                return -3;
        }

        ((circular_buffer_state_t *)
         *circular_buffer)->has_valid_entry = 0;

        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: write_circular_buffer
 *
 *
 * Arguments:
 *
 * void *circular_buffer: a pointer to the the circular buffer.
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 * void *data: data to put in the circular buffer.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * -2 if the call to get_entry_size fails
 *
 * -3 if the call to get_num_entries fails
 *
 *
 * Notes: This function implements the following pseudo-code:
 *
 * if (!has_valid_entry)
 *   write to slot 0
 *   begin := 0
 *   end := 0
 *   has_valid_entry := 1
 * else
 *   if (end + 1) % num_entries == begin
 *     begin := begin + 1 % num_entries
 *   write to slot (end + 1) % num_entries
 *   end := (end + 1) % num_entries
 *
 * Here are some examples:
 *
 * slot  0 1 2 3
 * begin |
 * end   |
 *
 *   write to slot 1
 *   end := 1
 *
 * slot  0 1 2 3
 * begin |
 * end     |
 *
 *   write to slot 2
 *   end := 2
 *
 * slot  0 1 2 3
 * begin |
 * end         |
 *
 *   b := 1
 *   write to slot 0
 *   end := 1
 *
 * ---------------------------------------------------------------- */

static int write_circular_buffer(void *circular_buffer,
                                 char id,
                                 void *data)
{
        /* state of circular buffer: */
        circular_buffer_state_t *state =
                (circular_buffer_state_t *) circular_buffer;

        /* base of slots of circular buffer: */
        void *slots_base =
                circular_buffer + sizeof(circular_buffer_state_t);

        unsigned entry_size = 0; /* number of bytes to write */

        unsigned num_entries;

        void *write_base; /* base address to write to */

        if (0 != valid_id(id))
        {
                return -1;
        }

        if (0 != get_entry_size(id, &entry_size))
        {
                printf("In write_circular_buffer: ");
                printf("error in call to get_entry_size\n");
                return -2;
        }

        if (0 != get_num_entries(id, &num_entries))
        {
                printf("In write_circular_buffer: ");
                printf("error in call to get_num_entries\n");
                return -3;
        }

        if (1 != state->has_valid_entry)
        {
                write_base = slots_base;
                memcpy(write_base, data, entry_size);
                state->begin = 0;
                state->end = 0;
                state->has_valid_entry = 1;
                return 0;
        }

        if ((state->end + 1) % num_entries == state->begin)
        {
                state->begin = (state->begin + 1) % num_entries;
        }
        write_base = slots_base +
                (entry_size * ((state->end + 1) % num_entries));
        memcpy(write_base, data, entry_size);
        state->end = (state->end + 1) % num_entries;
        return 0;
}

/* -------------------------------------------------------------------
 *
 * Function: read_circular_buffer
 *
 *
 * Arguments:
 *
 * void *circular_buffer: a pointer to the the circular buffer.
 *
 * char id: the type of the contents of the entries of the circular
 * buffer.
 *
 * void *data: upon succesful return data will point to the entry read
 * from the circular buffer.  The entry read will be, under most
 * circumstances, the latest complete entry written.
 *
 *
 * Returns:
 *
 * 0 upon success
 *
 * -1 if id is invalid
 *
 * -2 if the call to get_entry_size failed
 *
 * -3 if the circular buffer does not have a valid entry
 *
 * ---------------------------------------------------------------- */

static int read_circular_buffer(void *circular_buffer,
                                char id,
                                void *data)
{
        /* state of circular buffer: */
        circular_buffer_state_t *state =
                (circular_buffer_state_t *) circular_buffer;

        /* base of slots of circular buffer: */
        void *slots_base =
                circular_buffer + sizeof(circular_buffer_state_t);

        unsigned entry_size = 0; /* number of bytes to read */

        void *read_base; /* base address to read from */

        if (0 != valid_id(id))
        {
                return -1;
        }

        if (0 != get_entry_size(id, &entry_size))
        {
                printf("In read_circular_buffer: ");
                printf("error in call to get_entry_size\n");
                return -2;
        }

        if (1 != state->has_valid_entry)
        {
                return -3;
        }

        read_base = slots_base + (entry_size * state->end);
        memcpy(data, read_base, entry_size);
        return 0;
}

/* -------------------------------------------------------------------
 *
 * Variable: void *gps_buffer
 * Variable: void *ins_buffer
 * Variable: void *plant_outputs_buffer
 * Variable: void *plant_inputs_buffer
 *
 * These variables point to the circular buffers.  They are
 * initialized by the
 * writer_init_{gps,ins,plant_outputs,plant_inputs}_buffer functions
 * (for the writers) or by the
 * reader_init_{gps,ins,plant_outputs,plant_inputs}_buffer functions
 * (for the readers).
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
static void *gps_buffer;
static void *ins_buffer;
#endif

#ifdef PLANT
static void *plant_outputs_buffer;
static void *plant_inputs_buffer;
#endif

/* -------------------------------------------------------------------
 *
 * Function: writer_init_gps_buffer
 * Function: writer_init_ins_buffer
 * Function: writer_init_plant_outputs_buffer
 * Function: writer_init_plant_inputs_buffer
 *
 *
 * Returns:
 *
 * OK (0) upon success
 *
 * ERROR (-1) if the call to init_circular_buffer fails; this should
 * never occur if Ben has programmed correctly.
 *
 *
 * Notes: should be called by only the writer of the circular buffer
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
int writer_init_gps_buffer(void)
{
        if (0 != init_circular_buffer(&gps_buffer,
                                      GPS_MESG_ID))
        {
                printf("In writer_init_gps_buffer: ");
                printf("error in call to init_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef CONTROLLER
int writer_init_ins_buffer(void)
{
        if (0 != init_circular_buffer(&ins_buffer,
                                      INS_MESG_ID))
        {
                printf("In writer_init_ins_buffer: ");
                printf("error in call to init_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int writer_init_plant_outputs_buffer(void)
{
        if (0 != init_circular_buffer(&plant_outputs_buffer,
                                      PLANT_OUTPUTS_ID))
        {
                printf("In writer_init_plant_outputs_buffer: ");
                printf("error in call to init_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int writer_init_plant_inputs_buffer(void)
{
        if (0 != init_circular_buffer(&plant_inputs_buffer,
                                      PLANT_INPUTS_ID))
        {
                printf("In writer_init_plant_inputs_buffer: ");
                printf("error in call to init_circular_buffer\n");
                return ERROR;
        }
        return OK;
}
#endif

/* -------------------------------------------------------------------
 *
 * Function: reader_init_gps_buffer
 * Function: reader_init_ins_buffer
 * Function: reader_init_plant_outputs_buffer
 * Function: reader_init_plant_inputs_buffer
 *
 *
 * Returns:
 *
 * OK (0) upon success
 *
 * ERROR (-1) if the call to get_circular_buffer fails; this should
 * never occur if Ben has programmed correctly.
 *
 *
 * Notes: this function may be called by both the writer and the
 * readers of the circular buffer, but is intended to be called by
 * only the readers.
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
int reader_init_gps_buffer(void)
{
        if (0 != get_circular_buffer(&gps_buffer,
                                     GPS_MESG_ID))
        {
                printf("In reader_init_gps_buffer: ");
                printf("error in call to get_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef CONTROLLER
int reader_init_ins_buffer(void)
{
        if (0 != get_circular_buffer(&ins_buffer,
                                     INS_MESG_ID))
        {
                printf("In reader_init_ins_buffer: ");
                printf("error in call to get_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int reader_init_plant_outputs_buffer(void)
{
        if (0 != get_circular_buffer(&plant_outputs_buffer,
                                     PLANT_OUTPUTS_ID))
        {
                printf("In reader_init_plant_outputs_buffer: ");
                printf("error in call to get_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int reader_init_plant_inputs_buffer(void)
{
        if (0 != get_circular_buffer(&plant_inputs_buffer,
                                     PLANT_INPUTS_ID))
        {
                printf("In reader_init_plant_inputs_buffer: ");
                printf("error in call to get_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

/* -------------------------------------------------------------------
 *
 * Function: write_gps_buffer
 * Function: write_ins_buffer
 * Function: write_plant_outputs_buffer
 * Function: write_plant_inputs_buffer
 *
 *
 * Arguments:
 *
 * data: points to the data to be written
 *
 *
 * Returns:
 *
 * OK (0) upon success
 *
 * ERROR (-1) if the call to write_circular_buffer fails; this should
 * never occur if Ben has programmed correctly.
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
int write_gps_buffer(gps_mesg_t *data)
{
        if (0 != write_circular_buffer(gps_buffer,
                                       GPS_MESG_ID,
                                       data))
        {
                printf("In write_gps_buffer: ");
                printf("error in call to write_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef CONTROLLER
int write_ins_buffer(ins_mesg_t *data)
{
        if (0 != write_circular_buffer(ins_buffer,
                                       INS_MESG_ID,
                                       data))
        {
                printf("In write_ins_buffer: ");
                printf("error in call to write_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int write_plant_outputs_buffer(plant_outputs_t *data)
{
        if (0 != write_circular_buffer(plant_outputs_buffer,
                                       PLANT_OUTPUTS_ID,
                                       data))
        {
                printf("In write_plant_outputs_buffer: ");
                printf("error in call to write_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

#ifdef PLANT
int write_plant_inputs_buffer(plant_inputs_t *data)
{
        if (0 != write_circular_buffer(plant_inputs_buffer,
                                       PLANT_INPUTS_ID,
                                       data))
        {
                printf("In write_plant_inputs_buffer: ");
                printf("error in call to write_circular_buffer\n");
                return ERROR;
        }

        return OK;
}
#endif

/* -------------------------------------------------------------------
 *
 * Function: read_gps_buffer
 * Function: read_ins_buffer
 * Function: read_plant_outputs_buffer
 * Function: read_plant_inputs_buffer
 *
 *
 * Arguments:
 *
 * data: upon successful completion, the circular buffer entry will be
 * read into *data.
 *
 *
 * Returns:
 *
 * OK (0) upon success
 *
 * ERROR (-1) if the call to read_circular_buffer fails; this should
 * never occur if Ben has programmed correctly.
 *
 * NO_DATA (-2) if the circular buffer does not contain a valid entry;
 * this may occur if the writer hasn't written to the buffer.
 *
 * ---------------------------------------------------------------- */

#ifdef CONTROLLER
int read_gps_buffer(gps_mesg_t *data)
{
        int result = read_circular_buffer(gps_buffer,
                                          GPS_MESG_ID,
                                          data);
        if (0 != result && -3 != result)
        {
                printf("In read_gps_buffer: ");
                printf("error %d in call to read_circular_buffer\n",
                       result);
                return ERROR;
        }

        if (-3 == result)
        {
                return NO_DATA;
        }

        return OK;
}
#endif

#ifdef CONTROLLER
int read_ins_buffer(ins_mesg_t *data)
{
        int result = read_circular_buffer(ins_buffer,
                                          INS_MESG_ID,
                                          data);
        if (0 != result && -3 != result)
        {
                printf("In read_ins_buffer: ");
                printf("error %d in call to read_circular_buffer\n",
                       result);
                return ERROR;
        }

        if (-3 == result)
        {
                return NO_DATA;
        }

        return OK;
}
#endif

#ifdef PLANT
int read_plant_outputs_buffer(plant_outputs_t *data)
{
        int result = read_circular_buffer(plant_outputs_buffer,
                                          PLANT_OUTPUTS_ID,
                                          data);
        if (0 != result && -3 != result)
        {
                printf("In read_plant_outputs_buffer: ");
                printf("error %d in call to read_circular_buffer\n",
                       result);
                return ERROR;
        }

        if (-3 == result)
        {
                return NO_DATA;
        }

        return OK;
}
#endif

#ifdef PLANT
int read_plant_inputs_buffer(plant_inputs_t *data)
{
        int result = read_circular_buffer(plant_inputs_buffer,
                                          PLANT_INPUTS_ID,
                                          data);
        if (0 != result && -3 != result)
        {
                printf("In read_plant_inputs_buffer: ");
                printf("error %d in call to read_circular_buffer\n",
                       result);
                return ERROR;
        }

        if (-3 == result)
        {
                return NO_DATA;
        }

        return OK;
}
#endif

/* -------------------------------------------------------------------
 *
 * Function: main
 *
 * For testing this file.
 *
 *
 * Returns: 0
 *
 * ---------------------------------------------------------------- */

/*
int main(void)
{
        int i;

#ifdef CONTROLLER
        gps_mesg_t gps_mesg_write;
        gps_mesg_t gps_mesg_read;
        ins_mesg_t ins_mesg_write;
        ins_mesg_t ins_mesg_read;

        printf("writer_init_gps_buffer %d\n",
               writer_init_gps_buffer());
        printf("writer_init_ins_buffer %d\n",
               writer_init_ins_buffer());

        for (i=0; i<10; i++)
        {
                gps_mesg_write.this = i*2 + 0.0;
                gps_mesg_write.that = i*2 + 1.0;
                printf("write_gps_buffer %d\n",
                       write_gps_buffer(&gps_mesg_write));
                printf("read_gps_buffer %d\n",
                       read_gps_buffer(&gps_mesg_read));
                printf("%f %f\n",
                       gps_mesg_read.this,
                       gps_mesg_read.that);
        }

        for (i=0; i<10; i++)
        {
                ins_mesg_write.yes = i*3 + 0.0;
                ins_mesg_write.no = i*3 + 1.0;
                ins_mesg_write.maybe = i*3 + 2.0;
                printf("write_ins_buffer %d\n",
                       write_ins_buffer(&ins_mesg_write));
                printf("read_ins_buffer %d\n",
                       read_ins_buffer(&ins_mesg_read));
                printf("%f %f %f\n",
                       ins_mesg_read.yes,
                       ins_mesg_read.no,
                       ins_mesg_read.maybe);
        }
#endif

#ifdef PLANT
        plant_outputs_t plant_outputs_write;
        plant_outputs_t plant_outputs_read;
        plant_inputs_t plant_inputs_write;
        plant_inputs_t plant_inputs_read;

        printf("writer_init_plant_outputs_buffer %d\n",
               writer_init_plant_outputs_buffer());
        printf("writer_init_plant_inputs_buffer %d\n",
               writer_init_plant_inputs_buffer());

        for (i=0; i<10; i++)
        {
                plant_outputs_write.fern = i*4 + 0.0;
                plant_outputs_write.bush = i*4 + 1.0;
                plant_outputs_write.rose = i*4 + 2.0;
                plant_outputs_write.tree = i*4 + 3.0;
                printf("write_plant_outputs_buffer %d\n",
                       write_plant_outputs_buffer(&plant_outputs_write));
                printf("read_plant_outputs_buffer %d\n",
                       read_plant_outputs_buffer(&plant_outputs_read));
                printf("%f %f %f %f\n",
                       plant_outputs_read.fern,
                       plant_outputs_read.bush,
                       plant_outputs_read.rose,
                       plant_outputs_read.tree);
        }

        for (i=0; i<10; i++)
        {
                plant_inputs_write.daisy = i*5 + 0.0;
                plant_inputs_write.lilly = i*5 + 1.0;
                plant_inputs_write.holly = i*5 + 2.0;
                plant_inputs_write.grass = i*5 + 3.0;
                plant_inputs_write.shrub = i*5 + 4.0;
                printf("write_plant_inputs_buffer %d\n",
                       write_plant_inputs_buffer(&plant_inputs_write));
                printf("read_plant_inputs_buffer %d\n",
                       read_plant_inputs_buffer(&plant_inputs_read));
                printf("%f %f %f %f %f\n",
                       plant_inputs_read.daisy,
                       plant_inputs_read.lilly,
                       plant_inputs_read.holly,
                       plant_inputs_read.grass,
                       plant_inputs_read.shrub);
        }
#endif

        return 0;
}
*/
