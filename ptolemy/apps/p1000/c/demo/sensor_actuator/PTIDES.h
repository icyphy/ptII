#include "EnDecode.c"

#define BUFFSIZE 256

//Let's just assume these...
//HOWEVER IF NSECS IS NON-0, WE HAVE TO CHANGE THE PROCEDURE TO MAKE THE CALCULATION CORRECT!!!!!
#define MODEL_DELAY_SECS 0
#define MODEL_DELAY_NSECS 7000000
#define BOUNDED_DELAY_SECS 0
#define BOUNDED_DELAY_NSECS 5000000

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
} Actor;

typedef struct{
    int int_value;
} Value;

typedef struct{
    unsigned int secs;
    unsigned int nsecs;
    int microstep;
} Tag;

typedef struct{
    Value this_value;
    Tag this_tag;
    Tag real_tag;

    Actor* actor_from;
    Actor* actor_to_fire;
    //an event would only have 1 actor_to_fire
    //ACTOR* actor_to_fire2;
    
    //need to have a bit that tell us whether static timing analysis has been done
    int sta_done;
} Event;

//building the linked list
//where when inserting, we insert by time stamp
typedef struct el{
    Event this_event;
    struct el *next;
} Event_Link;

void actuator_fire(Actor*, Event*);
void clock_init(Actor*);
void computation_fire(Actor*, Event*);
void event_add(Event);
void event_insert(Event);
void event_pop();
void execute_event();
void firing_actor(Event*);
void merge_fire(Actor*, Event*);
void model_delay_fire(Actor*, Event*);
void processEvents();
void receive_init(Actor*);
void sensor_run(Actor*);
void sensor_init(Actor*);
void static_timing_analysis(Event*);
void thread_timed_interrupt();
void transmit_fire(Actor*, Event*);

//IN EnDecode.c
void encodeHwNsec( FPGA_TIME*, const unsigned int, const unsigned int);
void decodeHwNsec( const FPGA_TIME*, unsigned int*, unsigned int*);
  
