#include "EnDecode.c"

#define BUFFSIZE 256

//Let's just assume these...
//HOWEVER IF NSECS IS NON-0, WE HAVE TO CHANGE THE PROCEDURE TO MAKE THE CALCULATION CORRECT!!!!!
#define MODEL_DELAY_SECS 2
#define MODEL_DELAY_NSECS 0
#define BOUNDED_DELAY_SECS 1
#define BOUNDED_DELAY_NSECS 0

int static_adj_secs = BOUNDED_DELAY_SECS - MODEL_DELAY_SECS;
int static_adj_nsecs= BOUNDED_DELAY_NSECS- MODEL_DELAY_NSECS;

//ALL STRUCTs
typedef struct act {

    struct act *next_actor1;
    struct act *next_actor2;
    //int num_actors;
    //struct act[num_actors] *next_actors;

    char type[3];

    //actor methods
    //preinitialize();
    //initialize(){

    //prefire() returns true to indicate that the actor is ready to fire
    //prefire();
    //fire();
    //postfire();
    //wrapup();
} ACTOR;

typedef struct{
    int int_value;
} VALUE;

typedef struct{
    unsigned int secs;
    unsigned int nsecs;
    int microstep;
} TAG;

typedef struct{
    VALUE this_value;
    TAG this_tag;
    TAG real_tag;

    ACTOR* actor_from;
    ACTOR* actor_to_fire;
    //an event would only have 1 actor_to_fire
    //ACTOR* actor_to_fire2;
} EVENT;

//building the linked list
//where when inserting, we insert by time stamp
typedef struct el{
    EVENT this_event;
    struct el *next;
} EVENT_LINK;




//IN EnDecode.c
void encodeHwNsec( FPGA_TIME*, const unsigned int, const unsigned int);
void decodeHwNsec( const FPGA_TIME*, unsigned int*, unsigned int*);
  
