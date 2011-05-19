/** Utilities for the type infrastructure.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Jia Zou
 @Parts of this code that serves to interface the hardware is copied from
 previous written code samples by Slobodan Matic and Agilent

 */

#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <math.h>
#include <netinet/in.h>
#include <pthread.h>
#include <semaphore.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/stat.h>
/**
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/types.h>
*/
#include <unistd.h>

/**
 Include files are as defined here.
 */
#include "EnDecode.c"
#include "PTIDES.h"
#include "ptpHwP1000LinuxDr.h"

//Global varaibles are defined here.
int fd;

char* GLOBAL_PORT1;
char* GLOBAL_PORT2;
char* GLOBAL_SERVER;

//Semaphores are declared as global variables.
//sem_t process_sem;
sem_t eventQueueSem;

/**
 This boolean makes sure that processEvents() is never executed by more than 1 thread at a time
 FIXME: however this requires fix since checking and setting this boolean is not atomic
 */
int PROCESSING_EVENT = 0;

//The start of the queue is also set as a global variable.
//void event_init(){
Event_Link *EVENT_QUEUE_HEAD = NULL;
//}


/**
 * This is the fire method for actuator
 * The actuator is used to check if the event received output has met the deadline
 * by comparing the current time with the timestamp.
 * Note that it does actually use the hardware to do the actuation.
 * Since it's a terminating actor, the event is popped out of the event queue
 */
void actuator_fire(Actor* this_actuate, Event* thisEvent) {
    unsigned int secs;
    unsigned int nsecs;
    unsigned int microstep = 0;     //FIXME: DEAL WITH microsteps later.....
    int rtn;
    FPGA_GET_TIME fpgaGetTime;
    //Read the time from the log
    rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
    {
        fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
        perror("error from ioctl");
        exit(1);
    }
    // Decode
    decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);

    Tag *stampedTag = &(thisEvent->realTag);

    // compare time with taged time
    if ((stampedTag->secs > secs) || ((stampedTag->secs == secs) && (stampedTag->nsecs > nsecs)) 
		|| ((stampedTag->secs == secs) && (stampedTag->nsecs == nsecs) && (stampedTag->microstep > microstep)))
    {
        printf("\nthe actuator was able to produce event ON TIME!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stampedTag->secs, stampedTag->nsecs, stampedTag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }
    else
    {
        printf("\nthe timing of the system was NOT MET!!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stampedTag->secs, stampedTag->nsecs, stampedTag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }

    printf("\n");
    event_pop();
    
    return;
}

/**
 * This function is basically the same as clock_init(), expect only this 
 * function is called through an event, thus we use event_add() instead 
 * of event_insert() in this case.
 */
void clock_fire(Actor* this_clock, Event* thisEvent) {

    unsigned int secs = thisEvent->orderTag.secs;
    unsigned int nsecs = thisEvent->orderTag.nsecs;;
    unsigned int real_secs = secs;
    unsigned int real_nsecs = nsecs;
    unsigned int i;
    
    //Pop the event that results in clock_fire first.
    event_pop();

    for (i = 0; i < CLOCK_EVENTS; i++)
    {
        real_secs += CLOCK_PERIOD_SECS;
        real_nsecs += CLOCK_PERIOD_NSECS;

        Event new_event;

        //Notice orderTag and realTag are set to different things:
        //since the event queue is ordered by orderTag, we these events to 
		//execute now.
		//However realTag is the real timestamp to appear at the output.
        new_event.orderTag.secs = secs;
        new_event.orderTag.nsecs = nsecs;
        new_event.orderTag.microstep = 0;   //FIXME
        new_event.this_value.intValue = 0;
        new_event.realTag.secs = real_secs;
        new_event.realTag.nsecs = real_nsecs;
        new_event.realTag.microstep = 0;   //FIXME
        new_event.actorToFire = this_clock->nextActor1;
        new_event.actorFrom = this_clock;

        event_add(new_event);

        if (this_clock->nextActor2 != NULL) {
            Event new_event2 = new_event;
            new_event2.actorToFire = this_clock->nextActor2;
            event_add(new_event2);
        }
    }

    //Now add another event that will re-call clock_init()
    //in order to re-initiate the clock cycle
    //Here orderTag and actorToFire are updated, however not realTag.
    Event clock_event;
    clock_event.orderTag.secs = real_secs;
    clock_event.orderTag.nsecs = real_nsecs;
    clock_event.realTag.secs = real_secs;
    clock_event.realTag.nsecs = real_nsecs;
    clock_event.orderTag.microstep = clock_event.realTag.microstep;    //FIXME
    clock_event.realTag.microstep = clock_event.realTag.microstep;    //FIXME
    clock_event.actorFrom = NULL;
    clock_event.actorToFire = this_clock;

    event_add(clock_event);
    
    //Do not need to call processEvents() in this case
    //since processEvents() called clock_fire
    return;
}

/** This is the initilization for the clock actor.
 * its functionality is to provide events with intervals specified by 
 * CLOCK_PERIOD_SECS/NSECS.
 * At one time, CLOCK_EVNTS number of events are produced at a time, and they 
 * can be executed momentarily.
 * when the deadline of the last event that was produced has passed
 * CLOCK_EVENTS number of events are again produced by a event added into the event queue
 */
void clock_init(Actor *this_clock)
{

    FPGA_GET_TIME fpgaGetTime;
    int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
        {
            fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
            perror("error from ioctl");
            exit(1);
        }

    // Scale from HW to TAI nsec
    unsigned int secs;
    unsigned int nsecs;
    decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
    printf("Clock_init got this time: %.9d.%9.9d\n", secs, nsecs);

    unsigned int real_secs = secs;     
    unsigned int real_nsecs = nsecs;
    unsigned int i;
    for (i = 0; i < CLOCK_EVENTS; i++)
    {
        //CLOCK_PERIOD specifies the period
        real_secs += CLOCK_PERIOD_SECS;     
        real_nsecs += CLOCK_PERIOD_NSECS;

        Event new_event;

        //notice orderTag and realTag are set to different things:
        //since the event queue is ordered by orderTag, we these events to execute NOW
        //however realTag is the real timestamp to appear at the output
        new_event.orderTag.secs = secs;
        new_event.orderTag.nsecs = nsecs;
        new_event.orderTag.microstep = 0;   //FIXME
        new_event.this_value.intValue = 0;
        new_event.realTag.secs = real_secs;
        new_event.realTag.nsecs = real_nsecs;
        new_event.realTag.microstep = 0;   //FIXME
        new_event.actorToFire = this_clock->nextActor1;
        new_event.actorFrom = this_clock;

        event_insert(new_event);

        if (this_clock->nextActor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actorToFire = this_clock->nextActor2;
            event_insert(new_event2);
        }

    }

    //now add another event that will re-call clock_init() in order to re-initiate the clock cycle
    Event clock_event;
    clock_event.orderTag.secs = real_secs;
    clock_event.orderTag.nsecs = real_nsecs;
    clock_event.realTag.secs = real_secs;
    clock_event.realTag.nsecs = real_nsecs;
    clock_event.orderTag.microstep = clock_event.realTag.microstep;    //FIXME
    clock_event.realTag.microstep = clock_event.realTag.microstep;    //FIXME
    clock_event.actorFrom = NULL;
    clock_event.actorToFire = this_clock;
    
    event_insert(clock_event);

    processEvents();

    return;
}

/** 
 * This is the fire method for computation.
 * Computation method do not have any real functionality here, where we simply
 * set our event queue to fire the next actor.
 */
void computation_fire(Actor* this_computation, Event* thisEvent) {

/**    EVENT new_event = *thisEvent;
    
    new_event.orderTag.secs = thisEvent->orderTag.secs;
    new_event.orderTag.nsecs = thisEvent->orderTag.nsecs;
    new_event.orderTag.microstep = 0;       FIXME
    new_event.this_value.intValue = 0;
    new_event.realTag.secs = thisEvent->realTag.secs;
    new_event.realTag.nsecs = thisEvent->realTag.nsecs;
    new_event.realTag.microstep = thisEvent->realTag.microstep;
    new_event.actor_to_fire1 = this_computation->nextActor1;
    new_event.actor_to_fire2 = this_computation->nextActor2;
    new_event.actorFrom = this_computation;

    //we have to pop and insert events because we have modified orderTag
    event_pop();
    event_insert(new_event);
*/

    //note that we did not do event_pop() or event_add()
    //in order to save execution time.
    //we simply modified the next actor to fire
    //also, if the computation actually does something, it would modify the token here too
    thisEvent->actorToFire = this_computation->nextActor1;
    thisEvent->actorFrom = this_computation;
    
    if (this_computation->nextActor2 != NULL)
    {
        Event new_event2 = *thisEvent;
        new_event2.actorToFire = this_computation->nextActor2;
        event_add(new_event2);
    }

    return;
}

/**
 This function is called when an error occures.
 */
void Die(char *mess) { 
	perror(mess); 
	exit(1); 
}

/** 
 * The method event_add() is only used when an actor that's not the source 
 * within a platform.
 * In this case there's no need to lock the event queue since processEvents() 
 * only execute one event at a time.
 * When events are added, make sure the event queue is ordered by orderTag of the events.
 * Currently, we simply go through the queue one event at a time and check the tags.
 * FIXME: use either binary search or calendar to improve performance.
 */
void event_add(Event new_event)
{

    printf("adding new event\n");
    Tag stampedTag = new_event.orderTag;
    //add an event
    Event_Link *tmp = malloc(sizeof(Event_Link));
    Event_Link *compare_event = EVENT_QUEUE_HEAD;
    Event_Link *before_event = EVENT_QUEUE_HEAD;
    //move across the link until we find an event with larger tag, or stop at the end.
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (stampedTag.secs < compare_event->thisEvent.orderTag.secs)
                stop_flag = 1;
        else if ((stampedTag.secs == compare_event->thisEvent.orderTag.secs) && (stampedTag.nsecs < compare_event->thisEvent.orderTag.nsecs))
                stop_flag = 1;
        else if ((stampedTag.secs == compare_event->thisEvent.orderTag.secs) && (stampedTag.nsecs == compare_event->thisEvent.orderTag.nsecs) && (stampedTag.microstep < compare_event->thisEvent.orderTag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            
    tmp->next = compare_event;
    tmp->thisEvent = new_event;
    if (compare_event == before_event){
        EVENT_QUEUE_HEAD = tmp;
    }
    else if (compare_event != before_event){
        before_event->next = tmp;
    }
    else {
        printf("something wrong with compare and before _event\n");
        exit(1);
    }

    printf("done adding event\n");
}

/** event_insert() is called when an actor that's the source of a platform, 
 * i.e., sensor, receiver need to insert events, so it's interlocked with 
 * processEvents().
 * Otherwise it performs the exact function as event_add().
 */
void event_insert(Event new_event)
{
   
    printf("inserting new event\n");

    //lock event queue semaphore
    if (sem_wait(&eventQueueSem) == -1)
    {
        perror("sem_wait: eventQueueSem");
    }
        
    Tag stampedTag = new_event.orderTag;
    //insert an event
    Event_Link *tmp = malloc(sizeof(Event_Link));
    Event_Link *compare_event = EVENT_QUEUE_HEAD;
    Event_Link *before_event = EVENT_QUEUE_HEAD;

    //move across the link until we reach an event of bigger tag
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (stampedTag.secs < compare_event->thisEvent.orderTag.secs)
                stop_flag = 1;
        else if ((stampedTag.secs == compare_event->thisEvent.orderTag.secs) && (stampedTag.nsecs < compare_event->thisEvent.orderTag.nsecs))
                stop_flag = 1;
        else if ((stampedTag.secs == compare_event->thisEvent.orderTag.secs) && (stampedTag.nsecs == compare_event->thisEvent.orderTag.nsecs) && (stampedTag.microstep < compare_event->thisEvent.orderTag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            
    tmp->next = compare_event;
    tmp->thisEvent = new_event;
    if (compare_event == before_event){
        EVENT_QUEUE_HEAD = tmp;
    }
    else if (compare_event != before_event){
        before_event->next = tmp;
    }
    else {
        printf("something wrong with compare and before _event\n");
        exit(1);
    }
    
    //UNLOCK event queue semaphore
    if (sem_post(&eventQueueSem) == -1) {
        perror("sem_post: eventQueueSem");
        exit(1);
    }
    printf("done inserting new event\n");
}

/** 
 * event_pop() is called to pop the most recent event in the event queue.
 * it is not interlocked because it can only be called by sinks within a 
 * platform, which is the called by processEvents().
 * Here the first event on the event queue is popped.
 */
void event_pop()
{
    printf("popping event from queue\n");
    //SEMAPHORE
/**    if (sem_wait(&eventQueueSem) == -1)
    {
        perror("sem_wait2");
    }
*/

    if (EVENT_QUEUE_HEAD != NULL){
        Event_Link *event_free = EVENT_QUEUE_HEAD;
        EVENT_QUEUE_HEAD = EVENT_QUEUE_HEAD -> next;
        free(event_free);
    } 
    else printf("event queue is already empty\n");

    //UNLOCK
/**    if (sem_post(&eventQueueSem) == -1) {
        perror("sem_post2");
        exit(1);
    }
*/    
    printf("event poped out of queue\n");
}

/** 
 * execute_event() checks if the event is valid. If it is, then fire actor
 * is called.
 */
void execute_event(){

    if (EVENT_QUEUE_HEAD == NULL) {
        printf("EVENT_QUEUE_HEAD should never be NULL\n");
        exit(1);
    }
    else {
        if (EVENT_QUEUE_HEAD->thisEvent.actorToFire == NULL){
            printf("executing an event where the actors are NULL!!\n");
            exit(1);
        }
        else {
            firing_actor(&(EVENT_QUEUE_HEAD->thisEvent));
        }
    }
}

/** 
 * fire_actor checks if static timing analysis is needed.
 * if it is, static timing analysis is called, and returns
 * if it's not, firing method of the actor specified by the event is called
 */
void firing_actor(Event* current_event)
{

    Actor* fire_this = current_event->actorToFire;
/**  //FYI
    sensor1->type = "ss1";
    clock1->type = "ck1";
    computation1->type = "cp1";
    transmit1->type = "tr1";
    model_delay1->type = "md1";
    merge1->type = "mg1";
*/
    //check if static timing analysis is needed
    //this is the same as to say: we need to do STA when the event is:
    //the source of the event is not a sensor && 
    //the event is going into an merge actor --> FIXME: any other actor that we need to care about?
    if (((current_event->actorToFire->type[0] == 'm') && (current_event->actorToFire->type[1] == 'g') && (current_event->actorToFire->type[2] == '1')) 
		&& ((current_event->actorFrom->type[0] == 'c') && (current_event->actorFrom->type[1] == 'p') && (current_event->actorFrom->type[2] == '1')) 
		&& current_event->staticTimingAnalysisDone != 1) 
    {

        //USING STATIC TIMING ANALYSIS, OR WE CHANGE orderTag
        //ONLY FOR ActorS WITH MORE THAN ONE SOURCE
        static_timing_analysis(current_event);

        //when the timing analysis is done, we do not fire the actor in this run
        return;
    }
    else {

        printf("now firing actor:\n%c%c%c\n", fire_this->type[0], fire_this->type[1], fire_this->type[2]);

        if (fire_this->type[0] == 's' && fire_this->type[1] == 's' && fire_this->type[2] == '1')
        {
            printf("sensor1 is always a receiver, not fired this way. it only get init once in main!!! \n");
            exit(1);
        }
        else if (fire_this->type[0] == 't' && fire_this->type[1] == 'r' && fire_this->type[2] == '1')
        {
            transmit_fire(fire_this, current_event);
        }    
        else if (fire_this->type[0] == 'r' && fire_this->type[1] == 'c' && fire_this->type[2] == '1')
        {
            printf("receiver1 should never be fired this way, it only get init once in main!!! \n");
            exit(1);
        }
        else if (fire_this->type[0] == 'c' && fire_this->type[1] == 'k' && fire_this->type[2] == '1')
        {
            clock_fire(fire_this, current_event);
        }
        else if (fire_this->type[0] == 'm' && fire_this->type[1] == 'd' && fire_this->type[2] == '1')
        {
            model_delay_fire(fire_this, current_event);
        }
        else if (fire_this->type[0] == 'm' && fire_this->type[1] == 'g' && fire_this->type[2] == '1')
        {
            merge_fire(fire_this, current_event);
        }
        else if (fire_this->type[0] == 'c' && fire_this->type[1] == 'p' && fire_this->type[2] == '1')
        {
            computation_fire(fire_this, current_event);   
        }
        else if (fire_this->type[0] == 'a' && fire_this->type[1] == 'c' && fire_this->type[2] == '1')
        {
            actuator_fire(fire_this, current_event);
        }
        else {
            printf("I need to fire this but I don't know how!!!: %c%c%c\n", fire_this->type[0], fire_this->type[1], fire_this->type[2]);
            exit(1);
        }

        printf("done firing actors\n");

        return;
    }
}

/** 
 * This is the fire method for merge actor.
 * This firing method transfer the event at the input and put it to the output.
 * It also prints the current timestamp, and which actor it is from.
 * Since it is not a terminating actor, no need to pop event.
 * Only possibility to add event is when it has more than one output.
 * A merge actor may have more than one input.
 */
void merge_fire(Actor* this_merge, Event* thisEvent) {

    printf("THIS IS THE FINAL OUTPUT OF THE MERGE Actor: \n");
    printf("MAKE SURE THE TagS OF THESE EVENTS ARE IN ORDER: the tag on the current value are: %.9d.%9.9d %i \n", 
		thisEvent->realTag.secs, thisEvent->realTag.nsecs, thisEvent->realTag.microstep);
    printf("THIS OUTPUT WAS FROM Actor: %c%c%c\n", 
		thisEvent->actorFrom->type[0], thisEvent->actorFrom->type[1], thisEvent->actorFrom->type[2]);

    thisEvent->actorToFire = this_merge->nextActor1;
    thisEvent->actorFrom = this_merge;

    if (this_merge->nextActor2 != NULL)
    {
        Event new_event2 = *thisEvent;
        new_event2.actorToFire = this_merge->nextActor2;
        event_add(new_event2);
    }

    return;
}

/** 
 * This is the fire method for model_delay actor.
 * model_delay actor increase the current realTag timestamp by the the 
 * parameter MODEL_DELAY_SECS, where MODEL_DELAY_SECS is greater or equal 
 * to maximum delay from the source of the event to the sink.
 * At the same time, the orderTag is increased by BOUNDED_DELAY, since
 * this is the delay between the sensor actor and the merge actor.
 * Since timestamps are modified in this process, event_pop() and event_add() 
 * needs to be called.
 * Because model_delay is also a transmitter through the network, socket
 * programming is used.
 */
void model_delay_fire(Actor* this_model_delay, Event* thisEvent) {

    Event new_event = *thisEvent;

    new_event.realTag.secs = thisEvent->realTag.secs + MODEL_DELAY_SECS;
    new_event.realTag.nsecs = thisEvent->realTag.nsecs + MODEL_DELAY_NSECS;
    if (new_event.realTag.nsecs >= 1000000000)
    {
        new_event.realTag.secs++;
        new_event.realTag.nsecs -= 1000000000;
    }
    new_event.realTag.microstep = 0;   //FIXME

    new_event.orderTag.secs = thisEvent->orderTag.secs + BOUNDED_DELAY_SECS;
    new_event.orderTag.nsecs = thisEvent->orderTag.nsecs + BOUNDED_DELAY_NSECS;
    if (new_event.orderTag.nsecs >= 1000000000)
    {
        new_event.orderTag.secs++;
        new_event.orderTag.nsecs -= 1000000000;
    }
    new_event.orderTag.microstep = 0;   //FIXME

    new_event.actorToFire = this_model_delay->nextActor1;
    new_event.actorFrom = this_model_delay;

    event_pop();

    if (this_model_delay->nextActor2 != NULL)
    {
        Event new_event2 = new_event;
        new_event2.actorToFire = this_model_delay->nextActor2;
        event_add(new_event2);
    }
    event_add(new_event);

    //do socket programming to send a message to the sensor
    int sock;
    struct sockaddr_in echoserver;
    unsigned int echolen;
    char sendstr[BUFFER_SIZE];
    
    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOBAL_SERVER);  /** IP address */
    echoserver.sin_port = htons(atoi(GLOBAL_PORT2));       /** server port */

    //these elements of an EVENT must be written in order...
    sprintf(sendstr, "%d %.9d %9.9d %d %.9d %9.9d %d", new_event.this_value.intValue, 
		new_event.orderTag.secs, new_event.orderTag.nsecs, new_event.orderTag.microstep, 
		new_event.realTag.secs, new_event.realTag.nsecs, new_event.realTag.microstep);

    echolen = sizeof(sendstr);
    //fprintf(stdout, "size: %d, \n",echolen);
    
    if (sendto(sock, sendstr, echolen, 0,
        (struct sockaddr *) &echoserver,
        sizeof(echoserver)) != echolen)    
    {
        Die("Mismatch in number of sent bytes");    
    }

    printf("these values were sent to the sensor: %d %.9d.%9.9d %d %.9d.%9.9d %d\n", new_event.this_value.intValue, new_event.orderTag.secs, new_event.orderTag.nsecs, new_event.orderTag.microstep, new_event.realTag.secs, new_event.realTag.nsecs, new_event.realTag.microstep);

    //close socket when done
    close(sock);

    return;
}

/** processEvents() execute events in the event queue with respect to orderTag of each event
 * pseudo code:
processEvents(){
	if (PROCESSING == 0){
		PROCESSING = 1
		
		While (EVENT_QUEUE_HEAD != NULL){
			sem_lock(eventQueueSem);
			if (get_current_time() > (EVENT_QUEUE_HEAD-> tag))
				execute(*EVENT_QUEUE_HEAD)
			else {
				if (get_timestamp() != 0) {
					if (get_timestamp() > (EVENT_QUEUE_HEAD -> tag)) {
						set_timestamp(EVENT_QUEUE_HEAD -> tag);
					}
				} else {
					create_thread (thread_timed_interrupt());
					set_timestamp (EVENT_QUEUE_HEAD -> tag);
					sem_unlock(eventQueueSem);
					break;
			}
			sem_unlock();
		}
		PROCESSING = 0;
	}
}

 */
void processEvents()
{

    //  To ensure this function is thread safe, we make sure only one processEvents() IS BEING EXECUTED AT A TIME
    //  WHILE ANOTHER SENSOR MIGHT TRIGGER ANOTHER processEvents()
    //  IF ANOTHER processEvents() is currently being executed, we don't really need to have another being executed at the same time...
    
    printf("\nexecuting processEvents()\n");
    //FIXME: not atomic
    if (PROCESSING_EVENT == 0) {
        PROCESSING_EVENT = 1;
    //THE SEMAPHORE IS ACTUALLY NOT WHAT WE WANTED...
/**        if (sem_wait(&process_sem) == -1)
        {
            perror("sem_wait1");
        }
*/
        while (EVENT_QUEUE_HEAD != NULL) {
            printf("start executing another event\n");
            //lock event queue
            if (sem_wait(&eventQueueSem) == -1)
            {
                perror("sem_wait2");
            }
            printf("went through event queue semaphores\n");
            
            Tag *stampedTag = &(EVENT_QUEUE_HEAD -> thisEvent.orderTag);
            //get the current time
            unsigned int secs;
            unsigned int nsecs;
            unsigned int microstep = 0;     //FIXME: DEAL WITH microsteps
            int rtn;

            FPGA_GET_TIME fpgaGetTime;
            // Read the time from the log
            rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
            if (rtn)
            {
                fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
                perror("error from ioctl");
                exit(1);
            }
            // Decode
            decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);        //FIXME: get 0's for secs and nsecs..
            
            printf("current time is: %.9d.%9.9d\n", secs, nsecs);
            printf("stamped time is: %.9d.%9.9d\n", stampedTag->secs, stampedTag->nsecs);

            // compare current time with taged time from event
            if ((stampedTag->secs < secs) || ((stampedTag->secs == secs) && (stampedTag->nsecs < nsecs)) || 
				((stampedTag->secs == secs) && (stampedTag->nsecs == nsecs) && (stampedTag->microstep < microstep)))
            {
                printf("this event is safe to execute\n");
                //we can safely execute the current event
                execute_event();
            }
            else
            {
                printf("this event is not safe to execute, start another thread and wait\n");

                //now we need to set the timetrigger, however if a timetrigger already exists, we need to retrieve it.
                FPGA_GET_TIMESTAMP fpgaGetTimestamp;
                // Read a timestamp from the log
                rtn = ioctl(fd,  FPGA_IOC_GET_TIMESTAMP, &fpgaGetTimestamp);
                if (rtn)
                {
                    fprintf(stderr, "ioctl to get timestamp failed: %d, %d\n", rtn, errno);
                    perror("error from ioctl");
                    exit(1);
                }
                
                unsigned int last_stamp_secs;
                unsigned int last_stamp_nsecs;
                decodeHwNsec( &fpgaGetTimestamp.timeVal, &last_stamp_secs, &last_stamp_nsecs);
                printf("last timestamp was: %.9d.%9.9d\n", last_stamp_secs, last_stamp_nsecs);
                
                //FIXME: what do I do with microstep? what the priority between them?
                //if retrieved timestamp is larger than the pending one, then we need to reset time trigger
                //if not, we can just ignore it
                if (last_stamp_secs != 0 || last_stamp_nsecs != 0){
                    if ((last_stamp_secs > stampedTag->secs) || 
						(last_stamp_secs == stampedTag->secs && last_stamp_nsecs > stampedTag->nsecs)){
                        FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
                        fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
                        fpgaSetTimetrigger.force = 1; // DO force
                        encodeHwNsec( &fpgaSetTimetrigger.timeVal, stampedTag->secs, stampedTag->nsecs);

                        rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
                        if (rtn)
                        {
                            fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
                            perror("error from ioctl");
                            exit(1);
                        }
                        printf("replaced timetrigger\n");
                        //FIXME FIXME: what if during this loop, the timed interrupt came to be true? that thread would simply call processEvents() and because it is currently executing, it would simply exit.
                        //how do we ensure that this timed interrupt always gets executed?
                    }
                }
                else {
                
                    //add anther thread that listens for the next safe time to execute
                    //processEvents(), while we break out of the current processEvents()
                    pthread_t  p_thread;
                    //listen for the time triggered interrupt
                    int thr_id;
printf("creating thread\n");                    
                    thr_id = pthread_create(&p_thread, NULL, (void*)thread_timed_interrupt, (void*)NULL);
                    //the stampedTag gives us the information about when it's safe to execute them.
printf("thread created\n");                    
                    FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
                    fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
                    fpgaSetTimetrigger.force = 1; // DO force
                    encodeHwNsec( &fpgaSetTimetrigger.timeVal, stampedTag->secs, stampedTag->nsecs);

                    rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
                    if (rtn)
                    {
                        fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
                        perror("error from ioctl");
                        exit(1);
                    }
                    printf("set timetrigger\n");

                }
                
                //UNLOCK
                if (sem_post(&eventQueueSem) == -1)
                {
                    perror("sem_post: eventQueueSem");
                }
                printf("done unlocking\n"); 
                
                //now break out of the big loop
                break;

            }
            //UNLOCK
            if (sem_post(&eventQueueSem) == -1)
            {
                perror("sem_post: eventQueueSem");
            }
            printf("done unlocking\n");
        }
        //break out the while loop over to here
        //UNLOCK
/**
        if (sem_post(&process_sem) == -1)
        {
            perror("sem_post1");
        }
*/
        printf("allow another processEvents() \n");
        PROCESSING_EVENT = 0;
    }
    printf("all available events have been processed\n\n");
    return;
}

/** 
 * This is the initialization function for the receiver.
 * It sets up to listen for any event coming from the sensor platform.
 * When events arrive, it calls event_insert() and then processEvents()
 *
 * FIXME: should setup a different thread to listen when an event is received
 *
 */
void receive_init(Actor* this_receiver)
{
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFER_SIZE];
    unsigned int clientlen, serverlen;
    int received = 0;
    
    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /** Any IP address */
    echoserver.sin_port = htons(atoi(GLOBAL_PORT1));    /** server port */
    //NOTE that the transmitter and receiver make use of port1, port2 is reserved for another
    
    /** Bind the socket */
    serverlen = sizeof(echoserver);
    if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0) {
        Die("Failed to bind server socket at receive init");
    }

    do {
        // Receive a message from the client
        clientlen = sizeof(echoclient);
        printf("receiver waiting for data from transmitter\n");
        if ((received = recvfrom(sock, buffer, BUFFER_SIZE, 0,
		    (struct sockaddr *) &echoclient,
		     &clientlen)) < 0) 
        {
            Die("Failed to receive message");
        }
        //    fprintf(stderr, "Client connected: %s\n", inet_ntoa(echoclient.sin_addr));

        //decode the buffer string into event
        Event new_event;
      
        sscanf(buffer, "%d %d %d %d %d %d %d", &new_event.this_value.intValue, 
			&new_event.orderTag.secs, &new_event.orderTag.nsecs, &new_event.orderTag.microstep, 
			&new_event.realTag.secs, &new_event.realTag.nsecs, &new_event.realTag.microstep);
                
        printf("We received these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", new_event.this_value.intValue, 
			new_event.orderTag.secs, new_event.orderTag.nsecs, new_event.orderTag.microstep, 
			new_event.realTag.secs, new_event.realTag.nsecs, new_event.realTag.microstep);
        
        new_event.actorToFire = this_receiver->nextActor1;
        new_event.actorFrom = this_receiver;

        event_insert(new_event);

        if (this_receiver->nextActor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actorToFire = this_receiver->nextActor2;
            event_insert(new_event2);
        }
        processEvents();

    } while(1);

    close(sock);

    return;
}

/** 
 * This is the initialization method for sensor actor.
 * Initialize the sensor actor to setup receiving function through another thread.
 * create new event then call processEvent()
 */

void sensor_init(Actor* this_sensor)
{

    pthread_t  p_thread;
    //set up sensor_init to listen for the events from actuator platform
    int thr_id;
    thr_id = pthread_create(&p_thread, NULL, (void*)sensor_run, (void*)this_sensor);

    //FIXME: this hack allow the user to have enough time (3secs) to initialize the actuator platform.
    sleep(3);

    //get the current time
    FPGA_GET_TIME fpgaGetTime;
    int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
    {
        fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
        perror("error from ioctl");
	    exit(1);
    }

    // Scale from HW to TAI nsecs
    unsigned int secs;
    unsigned int nsecs;
    decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
    printf("Sensor_init got this time: %.9d.%9.9d\n", secs, nsecs);
    // Set a time trigger from now
    Event new_event;

    new_event.orderTag.secs = secs;
    new_event.orderTag.nsecs = nsecs;
    new_event.orderTag.microstep = 0;	//FIXME
    new_event.realTag.secs = secs;
    new_event.realTag.nsecs = nsecs;
    new_event.realTag.microstep = 0;	//FIXEM
    new_event.this_value.intValue = 0;
    new_event.actorToFire = this_sensor->nextActor1;
    new_event.actorFrom = this_sensor;

    //now put the created event into the queue
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
    event_insert(new_event);

    if (this_sensor->nextActor2 != NULL)
    {
        Event new_event2 = new_event;
        new_event2.actorToFire = this_sensor->nextActor2;
        event_insert(new_event2);
    }
    
    // the event has been added to the queue, now we need to execute the event queue
    processEvents();

    printf("End sensor_init\n");

    return;
}

/**
 * This function is a spinoff from sensor_init(), where it always listens for new events from the actuator platform.
 * When an event arrives, it creates a new event, calls processEvent(), then again wait for the next interrupt.
 * FIXME: what if before processEvents() finishes, another event came in... should create another thread to listen...
 * Note that event_insert() is used instead of event_add(), because this is a source actor within this platform
 */
void sensor_run(Actor* this_sensor)     //this listens for the next signal to sense again
{
    printf("running sensor_run\n");
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFER_SIZE];
    unsigned int clientlen, serverlen;  //echolen,
    int received = 0;

    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /** Any IP address */
    echoserver.sin_port = htons(atoi(GLOBAL_PORT2));    /** server port */
    //NOTE that the transmitter and receiver make use of port2
    
    /** Bind the socket */
    serverlen = sizeof(echoserver);
    if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0) {
        Die("Failed to bind server socket at sensor_run");
    }

    do {
        // listens for messages from the client
        clientlen = sizeof(echoclient);
        printf("waiting to receive from actuator side\n");
        if ((received = recvfrom(sock, buffer, BUFFER_SIZE, 0,
            (struct sockaddr *) &echoclient,
            &clientlen)) < 0)
        {
            Die("Failed to receive message");
        }
        printf("received a token from actuator\n");

        Event new_event;

        sscanf(buffer, "%d %d %d %d %d %d %d", &new_event.this_value.intValue, 
			&new_event.orderTag.secs, &new_event.orderTag.nsecs, &new_event.orderTag.microstep, 
			&new_event.realTag.secs, &new_event.realTag.nsecs, &new_event.realTag.microstep);

        printf("the sensor received these data: %d %.9d %9.9d %d %.9d %9.9d %d\n", new_event.this_value.intValue, 
			new_event.orderTag.secs, new_event.orderTag.nsecs, new_event.orderTag.microstep, 
			new_event.realTag.secs, new_event.realTag.nsecs, new_event.realTag.microstep);

        //update the value from the sensor
        new_event.this_value.intValue = 0;

        //make realTag the same as orderTag
        new_event.realTag.secs = new_event.orderTag.secs;
        new_event.realTag.nsecs = new_event.orderTag.nsecs;
        new_event.realTag.microstep = new_event.orderTag.microstep;
        new_event.actorFrom = this_sensor;
        new_event.actorToFire = this_sensor->nextActor1;

        event_insert(new_event);

        if (this_sensor->nextActor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actorToFire = this_sensor->nextActor2;
            event_insert(new_event2);
        }
        processEvents();

    } while (1);
    close(sock);
    printf("out of while(1) loop in sensor_run\n");
    return;
}

/** 
 * Static timing analysis is called to set the timestamp of the event by a 
 * specific amount in order to fire the event at an instance that ensures 
 * all events are exectued in order.
 */
void static_timing_analysis(Event* thisEvent) {

    printf("Doing static_timing_analysis\n");
    Event new_event = *thisEvent;

    //MODEL_DELAY - BOUNDED_DELAY > = 0 for system to be schedulable.
    int static_adj_secs = MODEL_DELAY_SECS - BOUNDED_DELAY_SECS;
    int static_adj_nsecs;
    if (MODEL_DELAY_NSECS >= BOUNDED_DELAY_NSECS) {
        static_adj_nsecs = MODEL_DELAY_NSECS - BOUNDED_DELAY_NSECS;
    } else {
        static_adj_nsecs = MODEL_DELAY_NSECS + 1000000000 - BOUNDED_DELAY_NSECS;
        static_adj_secs--;
    }

    printf("static_adj_tag = %.9d.%9.9d\n", static_adj_secs, static_adj_nsecs);

    if (thisEvent->realTag.secs > static_adj_secs){
        new_event.orderTag.secs = thisEvent->realTag.secs - static_adj_secs;
    }
    else {
        printf("static_adj_secs too large!!!\n");
        exit(1);
    }
    if (thisEvent->realTag.nsecs >= static_adj_nsecs)
        new_event.orderTag.nsecs = thisEvent->realTag.nsecs - static_adj_nsecs;
    else {
        new_event.orderTag.nsecs = thisEvent->realTag.nsecs + 1000000000 - static_adj_nsecs;
        new_event.orderTag.secs--;
    }
    printf("Statically adjusted time: %.9d.%9.9d\n", new_event.orderTag.secs, new_event.orderTag.nsecs);

    new_event.orderTag.microstep = 0;  //FIXME

    //set the bit that says that static timing analysis has already been done;
    new_event.staticTimingAnalysisDone = 1;

/**          new_event.this_value.value = 0;
            new_event.realTag.secs = thisEvent->realTag.secs;
            new_event.realTag.nsecs = thisEvent->realTag.nsecs;
            new_event.realTag.microstep = thisEvent->realTag.microstep;
            new_event.actor_to_fire1 = this_computation->nextActor1;
            new_event.actor_to_fire2 = this_computation->nextActor2;
            new_event.actorFrom = this_computation;
*/
            //we have to pop and insert events because we have modified orderTag
    event_pop();
    event_add(new_event);
    printf("finished static_timing_analysis\n");

    return;
}

/** 
 * This function is called when processEvents() decides that the current 
 * event cannot be safely executed, in which case a thread is setup to listen
 * for the timebomb in this function.
 * when the timebomb explodes, processEvents() is called since the event 
 * should now be safely executed
 */
void thread_timed_interrupt() {
    // Block until the next interrupt
    unsigned int status;
    do
    {
        //HOW DO I PASS THE fd TO THIS FUNCTION? --> by global variable
        int num = read( fd, &status, sizeof(status));
        if (num != sizeof( status))
        {
            fprintf(stderr, "Error reading status, %d\n", num);
            exit(1);
        }
    } while ((status & TIMEBOMB_0_FIRE) == 0); // Got it!

    //now we call processEvents() again
    processEvents();

    pthread_exit(NULL);
}

/** 
 * This is the fire method for transmit.
 * transmit simply transmit the current event to the receiver in the next 
 * platform, thus socket programming is used.
 * even though transmit is not the final sink, it is the sink within this 
 * platform, thus event_pop() is called at the end.
 */
void transmit_fire(Actor* fire_this, Event* thisEvent) {
    printf("firing transmit_fire\n");

    int sock;
    struct sockaddr_in echoserver;
    unsigned int echolen;
    char sendstr[BUFFER_SIZE];


    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOBAL_SERVER);  /** IP address */
    echoserver.sin_port = htons(atoi(GLOBAL_PORT1));       /** server port */

    //these elements of an Event must be written in order...
    printf("transmitting these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", thisEvent->this_value.intValue, 
		thisEvent->orderTag.secs, thisEvent->orderTag.nsecs, thisEvent->orderTag.microstep, 
		thisEvent->realTag.secs, thisEvent->realTag.nsecs, thisEvent->realTag.microstep);
  
    sprintf(sendstr, "%9d %.9d %9.9d %9d %.9d %9.9d %9d", thisEvent->this_value.intValue, 
		thisEvent->orderTag.secs, thisEvent->orderTag.nsecs, thisEvent->orderTag.microstep, 
		thisEvent->realTag.secs, thisEvent->realTag.nsecs, thisEvent->realTag.microstep);
 
    echolen = sizeof(sendstr);
    //printf("size of the sent string is: %d\n", echolen);
    
    if (sendto(sock, sendstr, echolen, 0,
        (struct sockaddr *) &echoserver,
        sizeof(echoserver)) != echolen) 
    {
        Die("Mismatch in number of sent bytes");
    }

    printf("events has been sent from transmitter \n");
    
    event_pop();

    //close socket after finish
    close(sock);

    return;
}


/**
 * main() has the following functionalities:
 * declare all actors
 * setup dependencies between the actors
 * intialize actors
 */

int main(int argc, char *argv[])
{
    GLOBAL_SERVER = argv[1];
    GLOBAL_PORT1 = argv[2];
    GLOBAL_PORT2 = argv[3];

    if (argc != 5) {
        fprintf(stderr, "USAGE: %s <server_ip> <port1> <port2> <s or a>\n", argv[0]);
        //s stands for sensor side
        //a stands for actuator side
        exit(1);
    }
 
    //**************************************************
    //Declare all actors
    
    Actor sensor1;
    Actor clock1;
    Actor transmit1;
    Actor receive1;
    Actor merge1;
    Actor model_delay1;
    Actor actuator1;
    Actor computation1;

    sensor1.type[0] = 's';
    sensor1.type[1] = 's';
    sensor1.type[2] = '1';

    clock1.type[0] = 'c';
    clock1.type[1] = 'k';
    clock1.type[2] = '1';

    computation1.type[0] = 'c';
    computation1.type[1] = 'p';
    computation1.type[2] = '1';

    transmit1.type[0] = 't';
    transmit1.type[1] = 'r';
    transmit1.type[2] = '1';

    receive1.type[0]= 'r';
    receive1.type[1]= 'c';
    receive1.type[2]= '1';

    model_delay1.type[0] = 'm';
    model_delay1.type[1] = 'd';
    model_delay1.type[2] = '1';

    merge1.type[0] = 'm';
    merge1.type[1] = 'g';
    merge1.type[2] = '1';

    actuator1.type[0] = 'a';
    actuator1.type[1] = 'c';
    actuator1.type[2] = '1';
    
    /**sensor1.type = (char[3])("ss1");
    clock1.type = "ck1";
    computation1.type = "cp1";
    transmit1.type = "tr1";
    receive1.type = "rc1";
    model_delay1.type = "md1";
    merge1.type = "mg1";
    actuator1.type = "ac1";
    */

    //Dependencies between all the actors
    sensor1.nextActor1 = &transmit1;
    sensor1.nextActor2 = NULL;
    //this is true, but we do not fire the next one
    //transmit1.nextActor1 = &receive1;
    transmit1.nextActor1 = NULL;
    transmit1.nextActor2 = NULL;
    receive1.nextActor1 = &model_delay1;
    receive1.nextActor2 = NULL;
    model_delay1.nextActor1 = &merge1;
    model_delay1.nextActor2 = NULL;
    //model_delay1.nextActor2 = &sensor1;
    //even this is true, we do not fire the next one
    clock1.nextActor1 = &computation1;
    clock1.nextActor2 = NULL;
    computation1.nextActor1 = &merge1;
    computation1.nextActor2 = NULL;
    merge1.nextActor1 = &actuator1;
    merge1.nextActor2 = NULL;
    actuator1.nextActor1 = NULL;
    actuator1.nextActor2 = NULL;
    
    //initialize the event queue
    //event_init();

    //POSIX semaphores
    //the semaphores are declared as global variables.
        
    //non zero to share semaphore between processes
    int pshared = 0;

    /**
    if (sem_init(&process_sem, pshared, 1) == -1){
        perror("sem_init: process_sem");
    }
    */

    if (sem_init(&eventQueueSem, pshared, 1) == -1){
        perror("sem_init: eventQueueSem");
    }

    char devFile[23] = "/dev/ptpHwP1000LinuxDr";
    devFile[23] = '\0';
    fd = open(devFile, O_RDWR);
    if (fd < 0){
        printf("Error opening device file \"%s\"\n", devFile);
        exit(1);
    }

    //initialize all actors
    //if a, it's an actuator
    //if s, it's a sensor
    //if neither, error
    if (*argv[4] == 's'){
        //sensor_run_init(&sensor1);
        sensor_init(&sensor1);
    }
    else if (*argv[4] == 'a'){
        clock_init(&clock1);
        receive_init(&receive1);
    }
    else
        printf("didn't not specify if this is an actuator or sensor!!!\n");


    //go idle
    printf("go to sleep\n");
    sleep(60);

    //remove semaphores
    //sem_destroy(&process_sem);
    sem_destroy(&eventQueueSem);
    
    close(fd);

    return 0;
}

