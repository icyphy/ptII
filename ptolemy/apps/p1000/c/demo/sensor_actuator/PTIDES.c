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

#include "ptpHwP1000LinuxDr.h"
#include "PTIDES.h"

//Global varaibles
int fd;

char* GLOB_PORT1;
char* GLOB_PORT2;
char* GLOB_SERV_IP;

//Declaring semaphores as global variables
//sem_t process_sem;
sem_t event_queue_sem;
//this makes sure that processEvents() is never executed by more than 1 thread at a time
int PROCESSING_EVENT = 0;

//the start of the queue is a global variable
//void event_init(){
Event_Link *EVENT_QUEUE_HEAD = NULL;
//}

void Die(char *mess) { 
	perror(mess); exit(1); 
}

/*****************************************************************************
 * the section below includes functions that would reside in each Actor
 */

/***
 * fire method for actuator
 * the actuator is used to check if the output has met the deadline
 * by comparing the current time with the timestamp
 * Since it's a terminating actor, the event is popped out of the event queue
 */

void actuator_fire(Actor* this_actuate, Event* this_event) {
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

    Tag *stamped_tag = &(this_event->real_tag);

    // compare time with taged time
    if ((stamped_tag->secs > secs) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs > nsecs)) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs == nsecs) && (stamped_tag->microstep > microstep)))
    {
        printf("\nthe actuator was able to produce event ON TIME!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stamped_tag->secs, stamped_tag->nsecs, stamped_tag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }
    else
    {
        printf("\nthe timing of the system was NOT MET!!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stamped_tag->secs, stamped_tag->nsecs, stamped_tag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }

    printf("\n");
    event_pop();
    
    return;
}

/** fire method for merge actor
 * merge actor may have more than one input
 * and merges events from two sources while maintaining the event sequence with the help of the Director
 * no need to add/pop events, only need to modify the actor_to_fire within the correct event in order to save resource
 * otherwise we could simply call event_pop() and event_add() in sequence
 */
void merge_fire(Actor* this_merge, Event* this_event) {

    //by merge, we simply output the event, GIVEN THE SCHEDULER HAS GIVEN US THE INPUT EVENTS IN ORDER TO TIME STAMP
    //the tags do not change
    //nor does the token
    //IN THIS CASE, WE SIMPLY OUTPUT THE RESULT, AND MAKE SURE THE TagS ARE IN ORDER
    printf("THIS IS THE FINAL OUTPUT OF THE MERGE Actor: \n");
    printf("MAKE SURE THE TagS OF THESE EVENTS ARE IN ORDER: the tag on the current value are: %.9d.%9.9d %i \n", this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);
    printf("THIS OUTPUT WAS FROM Actor: %c%c%c\n", this_event->actor_from->type[0], this_event->actor_from->type[1], this_event->actor_from->type[2]);

    this_event->actor_to_fire = this_merge->next_actor1;
    this_event->actor_from = this_merge;

    if (this_merge->next_actor2 != NULL)
    {
        Event new_event2 = *this_event;
        new_event2.actor_to_fire = this_merge->next_actor2;
        event_add(new_event2);
    }

    return;
}

/** fire method for model_delay actor
 * model_delay actor increase the current timestamp by the the parameter MODEL_DELAY_SECS
 * where MODEL_DELAY_SECS is >= maximum delay from the source of the event to the sink
 *
 * FIXME: this_tag and real_tag should be set to different values
 *
 * since timestamp is modified in this process, event_pop() and event_add() needs to be called
 * because model_delay is also a transmitter through the network, socket programming is used
 */
void model_delay_fire(Actor* this_model_delay, Event* this_event) {

    Event new_event = *this_event;

    new_event.real_tag.secs = this_event->real_tag.secs + MODEL_DELAY_SECS;
    new_event.real_tag.nsecs = this_event->real_tag.nsecs + MODEL_DELAY_NSECS;
    if (new_event.real_tag.nsecs >= 1000000000)
    {
        new_event.real_tag.secs++;
        new_event.real_tag.nsecs -= 1000000000;
    }
    new_event.real_tag.microstep = 0;   //FIXME

    new_event.this_tag.secs = this_event->this_tag.secs + MODEL_DELAY_SECS;
    new_event.this_tag.nsecs = this_event->this_tag.nsecs + MODEL_DELAY_NSECS;
    if (new_event.this_tag.nsecs >= 1000000000)
    {
        new_event.this_tag.secs++;
        new_event.this_tag.nsecs -= 1000000000;
    }
    new_event.this_tag.microstep = 0;   //FIXME

    new_event.actor_to_fire = this_model_delay->next_actor1;
    new_event.actor_from = this_model_delay;

    event_pop();

    if (this_model_delay->next_actor2 != NULL)
    {
        Event new_event2 = new_event;
        new_event2.actor_to_fire = this_model_delay->next_actor2;
        event_add(new_event2);
    }
    event_add(new_event);

    //do socket programming to send a message to the sensor
    int sock;
    struct sockaddr_in echoserver;
    unsigned int echolen;
    char sendstr[BUFFSIZE];
    
    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOB_SERV_IP);  /** IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT2));       /** server port */

    //these elements of an EVENT must be written in order...
    sprintf(sendstr, "%d %.9d %9.9d %d %.9d %9.9d %d", new_event.this_value.int_value, 
		new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, 
		new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);

    echolen = sizeof(sendstr);
    //fprintf(stdout, "size: %d, \n",echolen);
    
    if (sendto(sock, sendstr, echolen, 0,
        (struct sockaddr *) &echoserver,
        sizeof(echoserver)) != echolen)    
    {
        Die("Mismatch in number of sent bytes");    
    }

    printf("these values were sent to the sensor: %d %.9d.%9.9d %d %.9d.%9.9d %d\n", new_event.this_value.int_value, new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);

    //close socket when done
    close(sock);

    return;
}

/** fire method for transmit
 * transmit simply transmit the current event to the receiver in the next platform
 * even though transmit is not the final sink, it is the sink within this platform, thus event_pop() is called at the end
 */
void transmit_fire(Actor* fire_this, Event* this_event) {
    printf("firing transmit_fire\n");

    int sock;
    struct sockaddr_in echoserver;
    unsigned int echolen;
    char sendstr[BUFFSIZE];


    /** Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /** Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /** Clear struct */
    echoserver.sin_family = AF_INET;                  /** Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOB_SERV_IP);  /** IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT1));       /** server port */

    //these elements of an Event must be written in order...
    printf("transmitting these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", this_event->this_value.int_value, 
		this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, 
		this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);
  
    sprintf(sendstr, "%9d %.9d %9.9d %9d %.9d %9.9d %9d", this_event->this_value.int_value, 
		this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, 
		this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);
 
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

/** fire method for computation
 * computation method do not have any real functionality here
 * it simply changes the next actor_to_fire element
 */
void computation_fire(Actor* this_computation, Event* this_event) {

/**    EVENT new_event = *this_event;
    
    new_event.this_tag.secs = this_event->this_tag.secs;
    new_event.this_tag.nsecs = this_event->this_tag.nsecs;
    new_event.this_tag.microstep = 0;       FIXME
    new_event.this_value.int_value = 0;
    new_event.real_tag.secs = this_event->real_tag.secs;
    new_event.real_tag.nsecs = this_event->real_tag.nsecs;
    new_event.real_tag.microstep = this_event->real_tag.microstep;
    new_event.actor_to_fire1 = this_computation->next_actor1;
    new_event.actor_to_fire2 = this_computation->next_actor2;
    new_event.actor_from = this_computation;

    //we have to pop and insert events because we have modified this_tag
    event_pop();
    event_insert(new_event);
*/

    //note that we did not do event_pop() or event_add()
    //in order to save execution time.
    //we simply modified the next actor to fire
    //also, if the computation actually does something, it would modify the token here too
    this_event->actor_to_fire = this_computation->next_actor1;
    this_event->actor_from = this_computation;
    
    if (this_computation->next_actor2 != NULL)
    {
        Event new_event2 = *this_event;
        new_event2.actor_to_fire = this_computation->next_actor2;
        event_add(new_event2);
    }

    return;
}
/*********************************************************************************
 * The section below still consist of functions that reside in each Actor
 * however, these functions would require initializations at startup
 *
 */

/** initialization method for sensor actor
 * initialize the sensor actor to setup receiving function through another thread
 * create new event then call processEvent()
 */

void sensor_init(Actor* this_sensor)
{

    pthread_t  p_thread;
    //set up sensor_init to listen for the events from actuator platform
    int thr_id;
    thr_id = pthread_create(&p_thread, NULL, (void*)sensor_run, (void*)this_sensor);

    //FIXME: this hack allow the user to have enough time (3secs) to initialize the actuator platform
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

    new_event.this_tag.secs = secs;
    new_event.this_tag.nsecs = nsecs;
    new_event.this_tag.microstep = 0;	//FIXME
    new_event.real_tag.secs = secs;
    new_event.real_tag.nsecs = nsecs;
    new_event.real_tag.microstep = 0;	//FIXEM
    new_event.this_value.int_value = 0;
    new_event.actor_to_fire = this_sensor->next_actor1;
    new_event.actor_from = this_sensor;

    //now put the created event into the queue
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
    event_insert(new_event);

    if (this_sensor->next_actor2 != NULL)
    {
        Event new_event2 = new_event;
        new_event2.actor_to_fire = this_sensor->next_actor2;
        event_insert(new_event2);
    }
    
    // the event has been added to the queue, now we need to execute the event queue
    processEvents();

    printf("End sensor_init\n");

    return;
}

/*** run function for sensor
 * this function is a spinoff from sensor_init(), where it always listens for new events from the actuator platform
 * when an event arrives, it creates a new event, calls processEvent, then again wait for the next interrupt
 *
 * FIXME: what if before processEvents() finishes, another event came in... should create another thread to listen...
 *
 * note that event_insert() is used instead of event_add(), because this is a source actor within this platform
 */
void sensor_run(Actor* this_sensor)     //this listens for the next signal to sense again
{
    printf("running sensor_run\n");
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFSIZE];
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
    echoserver.sin_port = htons(atoi(GLOB_PORT2));    /** server port */
    //NOTE that the transmitter and receiver make use of port2
    
    /** Bind the socket */
    serverlen = sizeof(echoserver);
    if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0) {
        Die("Failed to bind server socket at sensor_run");
    }

    unsigned int secs;
    unsigned int nsecs;

    do {
        // listens for messages from the client
        clientlen = sizeof(echoclient);
        printf("waiting to receive from actuator side\n");
        if ((received = recvfrom(sock, buffer, BUFFSIZE, 0,
            (struct sockaddr *) &echoclient,
            &clientlen)) < 0)
        {
            Die("Failed to receive message");
        }
        printf("received a token from actuator\n");

        Event new_event;

        sscanf(buffer, "%d %d %d %d %d %d %d", &new_event.this_value.int_value, 
			&new_event.this_tag.secs, &new_event.this_tag.nsecs, &new_event.this_tag.microstep, 
			&new_event.real_tag.secs, &new_event.real_tag.nsecs, &new_event.real_tag.microstep);

        printf("the sensor received these data: %d %.9d %9.9d %d %.9d %9.9d %d\n", new_event.this_value.int_value, 
			new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, 
			new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);

        //update the value from the sensor
        new_event.this_value.int_value = 0;

        //make real_tag the same as this_tag
        new_event.real_tag.secs = new_event.this_tag.secs;
        new_event.real_tag.nsecs = new_event.this_tag.nsecs;
        new_event.real_tag.microstep = new_event.this_tag.microstep;
        new_event.actor_from = this_sensor;
        new_event.actor_to_fire = this_sensor->next_actor1;

        event_insert(new_event);

        if (this_sensor->next_actor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actor_to_fire = this_sensor->next_actor2;
            event_insert(new_event2);
        }
        processEvents();

    } while (1);
    close(sock);
    printf("out of while(1) loop in sensor_run\n");
    return;
}

/*** this is the initilization for the clock actor
 * its functionality is to provide events with intervals specified by CLOCK_PERIOD_SECS/NSECS
 * at one time, CLOCK_EVNTS number of events are produced at a time, and they can be executed momentarily
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

        //notice this_tag and real_tag are set to different things:
        //since the event queue is ordered by this_tag, we these events to execute NOW
        //however real_tag is the real timestamp to appear at the output
        new_event.this_tag.secs = secs;
        new_event.this_tag.nsecs = nsecs;
        new_event.this_tag.microstep = 0;   //FIXME
        new_event.this_value.int_value = 0;
        new_event.real_tag.secs = real_secs;
        new_event.real_tag.nsecs = real_nsecs;
        new_event.real_tag.microstep = 0;   //FIXME
        new_event.actor_to_fire = this_clock->next_actor1;
        new_event.actor_from = this_clock;

        event_insert(new_event);

        if (this_clock->next_actor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actor_to_fire = this_clock->next_actor2;
            event_insert(new_event2);
        }

    }

    //now add another event that will re-call clock_init() in order to re-initiate the clock cycle
    Event clock_event;
    clock_event.this_tag.secs = real_secs;
    clock_event.this_tag.nsecs = real_nsecs;
    clock_event.real_tag.secs = real_secs;
    clock_event.real_tag.nsecs = real_nsecs;
    clock_event.this_tag.microstep = clock_event.real_tag.microstep;    //FIXME
    clock_event.real_tag.microstep = clock_event.real_tag.microstep;    //FIXME
    clock_event.actor_from = NULL;
    clock_event.actor_to_fire = this_clock;
    
    event_insert(clock_event);

    processEvents();

    return;
}

/** this function is basically the same as clock_init()
 * only this function is called through an event, thus we use event_add() instead of event_insert() in this case
 */
void clock_fire(Actor* this_clock, Event* this_event) {

    unsigned int secs = this_event->this_tag.secs;
    unsigned int nsecs = this_event->this_tag.nsecs;;
    unsigned int real_secs = secs;
    unsigned int real_nsecs = nsecs;
    unsigned int i;
    
    //pop the event that results in clock_fire first
    event_pop();

    for (i = 0; i < CLOCK_EVENTS; i++)
    {
        //5 secs period between two clock events
        real_secs += 5;
        real_nsecs += 0;

        Event new_event;

        //notice this_tag and real_tag are set to different things:
        //since the event queue is ordered by this_tag, we these events to execute NOW
        //however real_tag is the real timestamp to appear at the output
        new_event.this_tag.secs = secs;
        new_event.this_tag.nsecs = nsecs;
        new_event.this_tag.microstep = 0;   //FIXME
        new_event.this_value.int_value = 0;
        new_event.real_tag.secs = real_secs;
        new_event.real_tag.nsecs = real_nsecs;
        new_event.real_tag.microstep = 0;   //FIXME
        new_event.actor_to_fire = this_clock->next_actor1;
        new_event.actor_from = this_clock;

        event_add(new_event);

        if (this_clock->next_actor2 != NULL) {
            Event new_event2 = new_event;
            new_event2.actor_to_fire = this_clock->next_actor2;
            event_add(new_event2);
        }
    }

    //now add another event that will re-call clock_init()
    //in order to re-initiate the clock cycle
    //need to update this_tag and actor_to_fire, however not real_tag.
    Event clock_event;
    clock_event.this_tag.secs = real_secs;
    clock_event.this_tag.nsecs = real_nsecs;
    clock_event.real_tag.secs = real_secs;
    clock_event.real_tag.nsecs = real_nsecs;
    clock_event.this_tag.microstep = clock_event.real_tag.microstep;    //FIXME
    clock_event.real_tag.microstep = clock_event.real_tag.microstep;    //FIXME
    clock_event.actor_from = NULL;
    clock_event.actor_to_fire = this_clock;

    event_add(clock_event);
    
    //do not need to call processEvents() in this case
    //since processEvents() called clock_fire
    return;
}

/** this is the initialization function for the receiver
 * it sets up to listen for any event coming from the sensor platform
 * when events arrive, it calls event_insert() and then processEvents()
 *
 * FIXME: should setup a different thread to listen when an event is received
 *
 */
void receive_init(Actor* this_receiver)
{
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFSIZE];
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
    echoserver.sin_port = htons(atoi(GLOB_PORT1));    /** server port */
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
        if ((received = recvfrom(sock, buffer, BUFFSIZE, 0,
		    (struct sockaddr *) &echoclient,
		     &clientlen)) < 0) 
        {
            Die("Failed to receive message");
        }
        //    fprintf(stderr, "Client connected: %s\n", inet_ntoa(echoclient.sin_addr));

        //decode the buffer string into event
        Event new_event;
      
        sscanf(buffer, "%d %d %d %d %d %d %d", &new_event.this_value.int_value, 
			&new_event.this_tag.secs, &new_event.this_tag.nsecs, &new_event.this_tag.microstep, 
			&new_event.real_tag.secs, &new_event.real_tag.nsecs, &new_event.real_tag.microstep);
                
        printf("We received these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", new_event.this_value.int_value, 
			new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, 
			new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);
        
        new_event.actor_to_fire = this_receiver->next_actor1;
        new_event.actor_from = this_receiver;

        event_insert(new_event);

        if (this_receiver->next_actor2 != NULL)
        {
            Event new_event2 = new_event;
            new_event2.actor_to_fire = this_receiver->next_actor2;
            event_insert(new_event2);
        }
        processEvents();

    } while(1);

    close(sock);

    return;
}

/******************************************************************************
 * the section below include functions that would be part of the PTIDES director
 * functionalities include:
 * scheduling of the event firings -- setup timed interrupt when event not ready to fire
 * static timing analysis
 * event_queue manipulations
 */

/** processEvents() execute events in the event queue with respect to this_tag of each event
 * pseudo code:
processEvents(){
	if (PROCESSING == 0){
		PROCESSING = 1
		
		While (EVENT_QUEUE_HEAD != NULL){
			sem_lock(event_queue_sem);
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
					sem_unlock(event_queue_sem);
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
            if (sem_wait(&event_queue_sem) == -1)
            {
                perror("sem_wait2");
            }
            printf("went through event queue semaphores\n");
            
            Tag *stamped_tag = &(EVENT_QUEUE_HEAD -> this_event.this_tag);
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
            printf("stamped time is: %.9d.%9.9d\n", stamped_tag->secs, stamped_tag->nsecs);

            // compare current time with taged time from event
            if ((stamped_tag->secs < secs) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs < nsecs)) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs == nsecs) && (stamped_tag->microstep < microstep)))
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
                    if ((last_stamp_secs > stamped_tag->secs) || (last_stamp_secs == stamped_tag->secs && last_stamp_nsecs > stamped_tag->nsecs)){
                        FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
                        fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
                        fpgaSetTimetrigger.force = 1; // DO force
                        encodeHwNsec( &fpgaSetTimetrigger.timeVal, stamped_tag->secs, stamped_tag->nsecs);

                        rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
                        if (rtn)
                        {
                            fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
                            perror("error from ioctl");
                            exit(1);
                        }
                        printf("replaced timetrigger\n");
                        //FIXME FIXME!!!!!: what if during this loop, the timed interrupt came to be true? that thread would simply call processEvents() and because it is currently executing, it would simply exit.
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
                    //the stamped_tag gives us the information about when it's safe to execute them.
printf("thread created\n");                    
                    FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
                    fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
                    fpgaSetTimetrigger.force = 1; // DO force
                    encodeHwNsec( &fpgaSetTimetrigger.timeVal, stamped_tag->secs, stamped_tag->nsecs);

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
                if (sem_post(&event_queue_sem) == -1)
                {
                    perror("sem_post: event_queue_sem");
                }
                printf("done unlocking\n"); 
                
                //now break out of the big loop
                break;

            }
            //UNLOCK
            if (sem_post(&event_queue_sem) == -1)
            {
                perror("sem_post: event_queue_sem");
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

/** execute_event() checks if the event is valid. If it is, then fire actors
 */
void execute_event(){

    if (EVENT_QUEUE_HEAD == NULL) {
        printf("EVENT_QUEUE_HEAD should never be NULL\n");
        exit(1);
    }
    else {
        if (EVENT_QUEUE_HEAD->this_event.actor_to_fire == NULL){
            printf("executing an event where the actors are NULL!!\n");
            exit(1);
        }
        else {
            firing_actor(&(EVENT_QUEUE_HEAD->this_event));
        }
    }
}

/** fire_actor checks if static timing analysis is needed.
 * if it is, STA is called, and returns
 * if it's not, firing method of the actor specified by the event is called
 */
void firing_actor(Event* current_event)
{

    Actor* fire_this = current_event->actor_to_fire;
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
    if (((current_event->actor_to_fire->type[0] == 'm') && (current_event->actor_to_fire->type[1] == 'g') && (current_event->actor_to_fire->type[2] == '1')) 
		&& ((current_event->actor_from->type[0] == 'c') && (current_event->actor_from->type[1] == 'p') && (current_event->actor_from->type[2] == '1')) 
		&& current_event->sta_done != 1) 
    {

        //USING STATIC TIMING ANALYSIS, OR WE CHANGE this_tag
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

/** this function is called when processEvents() decides that the current event cannot be safely executed
 * in which case a thread is setup to listen for the timebomb in this function
 * when the timebomb explodes, processEvents() is called since the event should now be safely executed
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

/** STA is called to set the timestamp of the event by a specific amount in order to fire the event at an instance
 * that ensures all events are exectued in order
 */
void static_timing_analysis(Event* this_event) {

    printf("Doing static_timing_analysis\n");
    Event new_event = *this_event;

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

    if (this_event->real_tag.secs > static_adj_secs){
        new_event.this_tag.secs = this_event->real_tag.secs - static_adj_secs;
    }
    else {
        printf("static_adj_secs too large!!!\n");
        exit(1);
    }
    if (this_event->real_tag.nsecs >= static_adj_nsecs)
        new_event.this_tag.nsecs = this_event->real_tag.nsecs - static_adj_nsecs;
    else {
        new_event.this_tag.nsecs = this_event->real_tag.nsecs + 1000000000 - static_adj_nsecs;
        new_event.this_tag.secs--;
    }
    printf("Statically adjusted time: %.9d.%9.9d\n", new_event.this_tag.secs, new_event.this_tag.nsecs);

    new_event.this_tag.microstep = 0;  //FIXME

    //set the bit that says that static timing analysis has already been done;
    new_event.sta_done = 1;

/**          new_event.this_value.value = 0;
            new_event.real_tag.secs = this_event->real_tag.secs;
            new_event.real_tag.nsecs = this_event->real_tag.nsecs;
            new_event.real_tag.microstep = this_event->real_tag.microstep;
            new_event.actor_to_fire1 = this_computation->next_actor1;
            new_event.actor_to_fire2 = this_computation->next_actor2;
            new_event.actor_from = this_computation;
*/
            //we have to pop and insert events because we have modified this_tag
    event_pop();
    event_add(new_event);
    printf("finished static_timing_analysis\n");

    return;
}

void event_add(Event new_event)
{

    printf("adding new event\n");
    Tag stamped_tag = new_event.this_tag;
    //add an event
    Event_Link *tmp = malloc(sizeof(Event_Link));
    Event_Link *compare_event = EVENT_QUEUE_HEAD;
    Event_Link *before_event = EVENT_QUEUE_HEAD;
    //move across the link until we find an event with larger tag, or stop at the end.
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (stamped_tag.secs < compare_event->this_event.this_tag.secs)
                stop_flag = 1;
        else if ((stamped_tag.secs == compare_event->this_event.this_tag.secs) && (stamped_tag.nsecs < compare_event->this_event.this_tag.nsecs))
                stop_flag = 1;
        else if ((stamped_tag.secs == compare_event->this_event.this_tag.secs) && (stamped_tag.nsecs == compare_event->this_event.this_tag.nsecs) && (stamped_tag.microstep < compare_event->this_event.this_tag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            
    tmp->next = compare_event;
    tmp->this_event = new_event;
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

void event_insert(Event new_event)
{
   
    printf("inserting new event\n");

    //lock event queue semaphore
    if (sem_wait(&event_queue_sem) == -1)
    {
        perror("sem_wait: event_queue_sem");
    }
        
    Tag stamped_tag = new_event.this_tag;
    //insert an event
    Event_Link *tmp = malloc(sizeof(Event_Link));
    Event_Link *compare_event = EVENT_QUEUE_HEAD;
    Event_Link *before_event = EVENT_QUEUE_HEAD;

    //move across the link until we reach an event of bigger tag
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (stamped_tag.secs < compare_event->this_event.this_tag.secs)
                stop_flag = 1;
        else if ((stamped_tag.secs == compare_event->this_event.this_tag.secs) && (stamped_tag.nsecs < compare_event->this_event.this_tag.nsecs))
                stop_flag = 1;
        else if ((stamped_tag.secs == compare_event->this_event.this_tag.secs) && (stamped_tag.nsecs == compare_event->this_event.this_tag.nsecs) && (stamped_tag.microstep < compare_event->this_event.this_tag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            
    tmp->next = compare_event;
    tmp->this_event = new_event;
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
    if (sem_post(&event_queue_sem) == -1) {
        perror("sem_post: event_queue_sem");
        exit(1);
    }
    printf("done inserting new event\n");
}

void event_pop()
{
    printf("popping event from queue\n");
    //SEMAPHORE
/**    if (sem_wait(&event_queue_sem) == -1)
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
/**    if (sem_post(&event_queue_sem) == -1) {
        perror("sem_post2");
        exit(1);
    }
*/    
    printf("event poped out of queue\n");
}

/***********************************************************************************
 * main() with functionalities:
 * declare all actors
 * setup dependencies between the actors
 * intialize actors
 */

int main(int argc, char *argv[])
{
    GLOB_SERV_IP = argv[1];
    GLOB_PORT1 = argv[2];
    GLOB_PORT2 = argv[3];

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
    sensor1.next_actor1 = &transmit1;
    sensor1.next_actor2 = NULL;
    //this is true, but we do not fire the next one
    //transmit1.next_actor1 = &receive1;
    transmit1.next_actor1 = NULL;
    transmit1.next_actor2 = NULL;
    receive1.next_actor1 = &model_delay1;
    receive1.next_actor2 = NULL;
    model_delay1.next_actor1 = &merge1;
    model_delay1.next_actor2 = NULL;
    //model_delay1.next_actor2 = &sensor1;
    //even this is true, we do not fire the next one
    clock1.next_actor1 = &computation1;
    clock1.next_actor2 = NULL;
    computation1.next_actor1 = &merge1;
    computation1.next_actor2 = NULL;
    merge1.next_actor1 = &actuator1;
    merge1.next_actor2 = NULL;
    actuator1.next_actor1 = NULL;
    actuator1.next_actor2 = NULL;
    
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

    if (sem_init(&event_queue_sem, pshared, 1) == -1){
        perror("sem_init: event_queue_sem");
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
    sem_destroy(&event_queue_sem);
    
    close(fd);

    return 0;
}