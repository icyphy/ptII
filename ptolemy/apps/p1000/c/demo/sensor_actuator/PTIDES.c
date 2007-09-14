#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <math.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>

/*
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/types.h>
*/
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <semaphore.h>
#include "ptpHwP1000LinuxDr.h"
#include "p1000_utils.h"
#include "PTIDES.h"

int fd;

char* GLOB_PORT2;
char* GLOB_PORT1;
char* GLOB_SERV_IP;

//Declaring semaphores as global variables
sem_t process_sem;
sem_t event_queue_sem;

//THE START OF THE QUEUE IS NOW A GLOBAL VARIABLE
//void event_init(){
EVENT_LINK *EVENT_QUEUE_HEAD = NULL;
//}

//THIS MAKES SURE THAT THE EVENT QUEUE IS NOT ACCESSED BY MORE THAN 1 THREAD AT A TIME
//int EVENT_QUEUE_OCC = 0;
int PROCESSING_EVENT = 0;

void Die(char *mess) { perror(mess); exit(1); }

void actuator_fire(ACTOR* this_actuate, EVENT* this_event)
{
    unsigned int secs;
    unsigned int nsecs;
    unsigned int microstep = 0;     //DEAL WITH microsteps later.....
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

    TAG *stamped_tag = &(this_event->real_tag);

    // compare time with taged time
    if ((stamped_tag->secs > secs) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs > nsecs)) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs == nsecs) && (stamped_tag->microstep > microstep)))
    {
        printf("\nthe actuator was able to produce event ON time!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stamped_tag->secs, stamped_tag->nsecs, stamped_tag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }
    else
    {
        printf("\nthe timing of the system was NOT met!!!!! \n");
        printf("the timestamped tag is: %.9d.%9.9d %i \n", stamped_tag->secs, stamped_tag->nsecs, stamped_tag->microstep);
        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);
    }

    printf("\n");
    event_pop();
    
    return;
}

void merge_fire(ACTOR* this_merge, EVENT* this_event)
{

    //by merge, we simply output the event, GIVEN THE SCHEDULER HAS GIVEN US THE INPUT EVENTS IN ORDER TO TIME STAMP
    //the tags do not change
    //nor does the token
    //IN THIS CASE, WE SIMPLY OUTPUT THE RESULT, AND MAKE SURE THE TAGS ARE IN ORDER
    printf("THIS IS THE FINAL OUTPUT OF THE MERGE ACTOR: \n");
    printf("MAKE SURE THE TAGS OF THESE EVENTS ARE IN ORDER: the tag on the current value are: \n%.9d.%9.9d %i \n", this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep);
    printf("THIS OUTPUT WAS FROM ACTOR: %c%c%c\n", this_event->actor_from->type[0], this_event->actor_from->type[1], this_event->actor_from->type[2]);

    this_event->actor_to_fire = this_merge->next_actor1;
    this_event->actor_from = this_merge;

    if (this_merge->next_actor2 != NULL)
    {
        EVENT new_event2 = *this_event;
        new_event2.actor_to_fire = this_merge->next_actor2;
        event_add(new_event2);
    }

    return;
}

void model_delay_fire(ACTOR* this_model_delay, EVENT* this_event)
{

    //THIS IS DONE BY STATIC TIMING ANALYSIS... 
    EVENT new_event;

    new_event.real_tag.secs = this_event->real_tag.secs + MODEL_DELAY_SECS;
    new_event.real_tag.secs = this_event->real_tag.nsecs+ MODEL_DELAY_NSECS;
    if (new_event.real_tag.nsecs >= 1000000000)
    {
        new_event.real_tag.secs++;
        new_event.real_tag.nsecs -= 1000000000;
    }
    new_event.real_tag.microstep = 0;

    new_event.this_tag.secs = this_event->this_tag.secs + MODEL_DELAY_SECS;
    new_event.this_tag.secs = this_event->this_tag.nsecs+ MODEL_DELAY_NSECS;
    if (new_event.this_tag.nsecs >= 1000000000)
    {
        new_event.this_tag.secs++;
        new_event.this_tag.nsecs -= 1000000000;
    }
    new_event.this_tag.microstep = 0;

    new_event.actor_to_fire = this_model_delay->next_actor1;
    new_event.actor_from = this_model_delay;

    event_pop();

    if (this_model_delay->next_actor2 != NULL)
    {
        EVENT new_event2 = new_event;
        new_event.actor_to_fire = this_model_delay->next_actor2;
        event_add(new_event2);
    }

    //here since we only modified the event in the event queue, and since this_tag is not changed
    event_add(new_event);

    //now we do socket programming to send a message to the sensor
    int sock;
    struct sockaddr_in echoserver;
    unsigned int echolen, clientlen;
    char sendstr[BUFFSIZE];
    
        /* Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /* Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
    echoserver.sin_family = AF_INET;                  /* Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOB_SERV_IP);  /* IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT2));       /* server port */

    //these elements of an EVENT must be written in order...
    sprintf(sendstr, ". %d . %.9d . %9.9d . %d . %.9d . %9.9d . %d", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);


    //sprintf(sendstr, "value: %d, this_secs: %.9d this_nsecs: .%9.9d this_microstep: %d real_secs: %.9d real_nsecs: .%9.9d real_microstep: %d", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);

    echolen = sizeof(sendstr);
    fprintf(stdout, "size: %d, \n",echolen);
    
    //send these elements of the event
    //INFO ABOUT THE ACTORS CAN BE DONE ANOTHER WAY, IT'S IGNORED HERE...
    if (sendto(sock, sendstr, echolen, 0,
        (struct sockaddr *) &echoserver,
        sizeof(echoserver)) != echolen)    
    {
        Die("Mismatch in number of sent bytes");    
    }
    printf("events has been sent from transmitter \n");

    //here event_pop() cannot happen because this actor sends to the merge actor also...

    return;
}

void transmit_fire(ACTOR* fire_this, EVENT* this_event)
{
    printf("firing transmit_fire\n");

    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFSIZE];
    unsigned int echolen, clientlen;
    int received = 0;
    char sendstr[BUFFSIZE];
    //BUT WHAT IS THE REAL SIZE OF sendstr???

    /* Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /* Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
    echoserver.sin_family = AF_INET;                  /* Internet/IP */
    echoserver.sin_addr.s_addr = inet_addr(GLOB_SERV_IP);  /* IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT1));       /* server port */

    //these elements of an EVENT must be written in order...
    printf("transmitting these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);

    //sprintf(sendstr, ". %d . %.9d . %9.9d . %9d . %.9d . %9.9d . %d", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);
    
    sprintf(sendstr, "%9d %.9d %9.9d %9d %.9d %9.9d %9d", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);

    //sprintf(sendstr, "value: %d, this_secs: %.9d this_nsecs: .%9.9d this_microstep: %d real_secs: %.9d real_nsecs: .%9.9d real_microstep: %d", this_event->this_value.int_value, this_event->this_tag.secs, this_event->this_tag.nsecs, this_event->this_tag.microstep, this_event->real_tag.secs, this_event->real_tag.nsecs, this_event->real_tag.microstep);
    
    echolen = sizeof(sendstr);
    printf("size of the sent string is: %d\n", echolen);
    fprintf(stdout, "size: %d\n",echolen);
    
    //send these elements of the event
    //INFO ABOUT THE ACTORS CAN BE DONE ANOTHER WAY, IT'S IGNORED HERE...
    if (sendto(sock, sendstr, echolen, 0,
        (struct sockaddr *) &echoserver,
        sizeof(echoserver)) != echolen) 
    {
        Die("Mismatch in number of sent bytes");
    }

    printf("events has been sent from transmitter \n");
    
    //FIXME!!   FIXED
    //I cannot pop event here, because it might fire the next pointer (if there's an actor_to_fire2)
    //now each event can only fire one actor, so we can do this now
    event_pop();

    return;
}

void execute_event(){

    if (EVENT_QUEUE_HEAD == NULL)
        printf("EVENT_QUEUE_HEAD should never be NULL\n");
    else
    {
        ACTOR* fire_this = EVENT_QUEUE_HEAD->this_event.actor_to_fire;

        if (fire_this == NULL){
            printf("executing an event where the actors are NULL!!\n");
        }
        else {
            firing_actor(fire_this, &(EVENT_QUEUE_HEAD->this_event));
        }

    }


}


void firing_actor(ACTOR* fire_this, EVENT* current_event)
{

    //DURING POST-FIRE, IT HAS TO BE CHECKED WHETHER IT IS SAFE TO PROCESS IT NOW
    //USING STATIC TIMING ANALYSIS, OR WE CHANGE this_tag
    //ONLY FOR ACTORS WITH MORE THAN ONE SOURCE
    static_timing_analysis(current_event);

    printf("now firing actor:\n%c%c%c\n", fire_this->type[0], fire_this->type[1], fire_this->type[2]);
    //printf("i just fired this: %c \n", fire_this->type[1]);
/*
    sensor1->type = "ss1";
    clock1->type = "ck1";
    computation1->type = "cp1";
    transmit1->type = "tr1";
    model_delay1->type = "md1";
    merge1->type = "mg1";
*/
    if (fire_this->type[0] == 's' && fire_this->type[1] == 's' && fire_this->type[2] == '1')
    {
        printf("sensor1 is always a receiver, not fired this way. it only get init once in main!!! \n");
    }
    else if (fire_this->type[0] == 't' && fire_this->type[1] == 'r' && fire_this->type[2] == '1')
    {
        transmit_fire(fire_this, current_event);
    }    
    else if (fire_this->type[0] == 'r' && fire_this->type[1] == 'c' && fire_this->type[2] == '1')
    {
        printf("receiver1 should never be fired this way, it only get init once in main!!! \n");
    }
    else if (fire_this->type[0] == 'c' && fire_this->type[1] == 'k' && fire_this->type[2] == '1')
    {
        printf("clock1 should never be fired this way, it only gets init once in main!!! \n");
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
    else
        printf("I need to fire this but I don't know how!!!: %c%c%c\n", fire_this->type[0], fire_this->type[1], fire_this->type[2]);

    printf("done firing actors\n");

}

void thread_timed_interrupt()
{
    unsigned int secs;
    unsigned int nsecs;
    int rtn;

    // Block until the next interrupt
    unsigned int status;
    do
    {
        //HOW DO I PASS THE fd TO THIS FUNCTION?
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

void processEvents()
{
    //how do i make it thread safe...???
    //while there are events in the queue, we do:
    //  we check if the next event to be executed has a time trigger <= current time
    //  if yes, we execute this next event
    //  if not, then we set up a trigger for the time to execute that event.
    //
    //  THIS ALSO ENSURES THAT ONLY one processEvents() IS BEING EXECUTED AT A TIME
    //  OTHER THAN THAT ANOTHER SENSOR MIGHT TRIGGER ANOTHER processEvents()
    //  HOWEVER, IF ANOTHER processEvents() is currently being executed, we don't really need to have another being executed at the same time...
    
    
    printf("\nexecuting processEvents()\n");
    if (PROCESSING_EVENT == 0) {
        PROCESSING_EVENT = 1;
    //THE SEMAPHORE IS ACTUALLY NOT WHAT WE WANTED...
        //LOCK 1
/*        if (semop(semid, &sb1, 1) == -1) {
            perror("semop1");
            exit(1);
        }
*/
/*        if (sem_wait(&process_sem) == -1)
        {
            perror("sem_wait1");
        }
*/
        while (EVENT_QUEUE_HEAD != NULL) {
            printf("start executing another event\n");
            //LOCK 2
            /*
            if (semop(semid, &sb2, 1) == -1) {
                perror("semop2");
                exit(1);
            }
            */
            //lock event queue
            if (sem_wait(&event_queue_sem) == -1)
            {
                perror("sem_wait2");
            }
            //EVENT_QUEUE_OCC = 1;
            printf("went through event queue semaphores\n");
            
            TAG *stamped_tag = &(EVENT_QUEUE_HEAD -> this_event.real_tag);
            //printf("%c\n", EVENT_QUEUE_HEAD->this_event.actor_to_fire1->type[0]);
            //get the current time
            unsigned int secs;
            unsigned int nsecs;
            unsigned int microstep = 0;     //DEAL WITH microsteps later.....
            int rtn;

/*            int fd2;
    char devFile[23] = "/dev/ptpHwP1000LinuxDr";
    devFile[23] = '\0';
    fd2 = open(devFile, O_RDWR);
    if (fd2 < 0)
    {
        printf("Error opening device file \"%s\"\n", devFile);
        exit(1);
    }
    */
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

            // compare current time with taged time
            if ((stamped_tag->secs < secs) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs < nsecs)) || ((stamped_tag->secs == secs) && (stamped_tag->nsecs == nsecs) && (stamped_tag->microstep < microstep)))
            {
                printf("this event is safe to execute\n");
                //we can safely execute the current event
                execute_event();
                // by firing this actor
/*                ACTOR* fire_this = EVENT_QUEUE_HEAD->this_event.actor_to_fire1;
                if (fire_this != NULL){
                    firing_actor(fire_this, &(EVENT_QUEUE_HEAD->this_event));
                }
                printf("here2\n");
                if (EVENT_QUEUE_HEAD != NULL){      //THIS IS WRONG!!!!!FIXME!!!!
                if ((EVENT_QUEUE_HEAD->this_event.actor_to_fire2) != NULL){
                    printf("here3\n");
                    fire_this = EVENT_QUEUE_HEAD -> this_event.actor_to_fire2;
                    printf("here4\n");
                    firing_actor(fire_this, &(EVENT_QUEUE_HEAD->this_event));
                }
                printf("here5\n");
                }
*/
            }
            else
            {
                printf("this event is not safe to execute, start another thread and wait\n");
                //add anther thread that listens for the next safe time to execute, and then call processEvents(), while we break out of the current processEvents()
                pthread_t  p_thread;
                //listen for the next interrupt
                int thr_id = pthread_create(&p_thread, NULL, thread_timed_interrupt, (void*)NULL);
                //the stamped_tag gives us the information about when it's safe to execute them.
                secs = stamped_tag->secs;
                nsecs = stamped_tag->nsecs;
                FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
                fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
                fpgaSetTimetrigger.force = 1; // Don't force
                encodeHwNsec( &fpgaSetTimetrigger.timeVal, secs, nsecs);

                rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
                if (rtn)
                {
                    fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
                    perror("error from ioctl");
                    exit(1);
                }
                //UNLOCK
                if (sem_post(&event_queue_sem) == -1)
                {
                    perror("sem_post2");
                }
                printf("done unlocking\n"); 
                
                //now break out of the big loop
                break;

            }
            printf("trying to unlock\n");
            //UNLOCK
            if (sem_post(&event_queue_sem) == -1)
            {
                perror("sem_post2");
            }
            printf("done unlocking\n");
/*            sb2.sem_op = 1; // free resource
            if (semop(semid, &sb2, 1) == -1) {
                perror("semop2");
                exit(1);
            }
*/            //EVENT_QUEUE_OCC = 0;
        }
        //UNLOCK
/*
        if (sem_post(&process_sem) == -1)
        {
            perror("sem_post1");
        }
*/
/*        sb1.sem_op = 1; // free resource
        if (semop(semid, &sb1, 1) == -1) {
            perror("semop1");
            exit(1);
        }
*/      

        PROCESSING_EVENT = 0;
    }
    printf("all available events have been processed\n\n");
    return;
}

void sensor_run(ACTOR* this_sensor)     //this listens for the next signal to sense again
{
    printf("firing sensor_run\n");
    //here we receive...
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFSIZE];
    unsigned int echolen, clientlen, serverlen;
    int received = 0;
    int recvint;

    /* Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /* Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
    echoserver.sin_family = AF_INET;                  /* Internet/IP */
    echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /* Any IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT2));       /* server port */
    //NOTE that the transmitter and receiver make use of port2
    //
    /* Bind the socket */
    serverlen = sizeof(echoserver);
    if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0) {
        Die("Failed to bind server socket");
    }

    //int buff_size;

    do {
        // Receive a message from the client
        clientlen = sizeof(echoclient);
        printf("waiting to receive from actuator\n");
        if ((received = recvfrom(sock, buffer, BUFFSIZE, 0,
            (struct sockaddr *) &echoclient,
            &clientlen)) < 0)
        {
            Die("Failed to receive message");
        }
        printf("received a token from actuator\n");

        FPGA_GET_TIME fpgaGetTime;
        int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
        if (rtn)
        {
            fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
            perror("error from ioctl");
            exit(1);
            //continue;
        }

        // Scale from HW to TAI nsecs
        unsigned int secs;
        unsigned int nsecs;
        decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
        printf("Sensor_run got this time: %.9d.%9.9d\n", secs, nsecs);
        // Set a time trigger from now
        EVENT new_event;

        new_event.this_tag.secs = secs;
        new_event.this_tag.nsecs = nsecs;
        new_event.this_tag.microstep = 0;  //this isn't right. it's not always 0.
        new_event.real_tag.secs = secs;
        new_event.real_tag.nsecs = nsecs;
        new_event.real_tag.microstep = 0;
        new_event.this_value.int_value = 0;
        new_event.actor_to_fire = this_sensor->next_actor1;
        new_event.actor_from = this_sensor;

        event_insert(new_event);

        if (this_sensor->next_actor2 != NULL)
        {
            EVENT new_event2;
            new_event2.actor_to_fire = this_sensor->next_actor2;
            event_insert(new_event2);
        }
        processEvents();

    } while (1);
    printf("out of while(1) loop in receive_run\n");
    return;
}


void sensor_init(ACTOR* this_sensor)
{
    //get the current time
    FPGA_GET_TIME fpgaGetTime;
    int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
    {
        fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
        perror("error from ioctl");
	    exit(1);

	    //continue;
    }

    // Scale from HW to TAI nsecs
    unsigned int secs;
    unsigned int nsecs;
    decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
    printf("Sensor_init got this time: %.9d.%9.9d\n", secs, nsecs);
    // Set a time trigger from now
    EVENT new_event;

    new_event.this_tag.secs = secs;
    new_event.this_tag.nsecs = nsecs;
    new_event.this_tag.microstep = 0;  //this isn't right. it's not always 0.
    new_event.real_tag.secs = secs;
    new_event.real_tag.nsecs = nsecs;
    new_event.real_tag.microstep = 0;
    new_event.this_value.int_value = 0;
    new_event.actor_to_fire = this_sensor->next_actor1;
    new_event.actor_from = this_sensor;

    //Now I need to put the output event into the queue...
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
    //event_pop();
    event_insert(new_event);

    if (this_sensor->next_actor2 != NULL)
    {
        EVENT new_event2 = new_event;
        new_event2.actor_to_fire = this_sensor->next_actor2;
        event_insert(new_event2);
    }
    
    // the event has been added to the queue, now we need to execute the event queue
    processEvents();
    //now setup a thread that always listens to the next time sensor can produce an output
    
    pthread_t  p_thread;
    //listen for the next interrupt
    int thr_id = pthread_create(&p_thread, NULL, sensor_run, (void*)this_sensor);

    printf("End sensor_init\n");

    return;
}

void clock_init(ACTOR *this_clock)
{
    //generate these much clock events...
    unsigned int CLOCK_EVENTS = 10;

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
    printf("Get Time: %.9d.%9.9d\n", secs, nsecs);

    unsigned int real_secs = secs + 5;     //give a 5 sec delay...
    unsigned int real_nsecs = nsecs;
    unsigned int i = 0;
    for (i = 0; i < CLOCK_EVENTS; i++)
    {
        // Set a time trigger 10 seconds from now
        EVENT new_event;

        //WHAT SHOULD BE this_tag AND real_tag BE??
        new_event.this_tag.secs = secs;
        new_event.this_tag.nsecs = nsecs;
        new_event.this_tag.microstep = 0;  //this isn't right. can't be positive that it's 0 for now.
        new_event.this_value.int_value = 0;
        new_event.real_tag.secs = real_secs;
        new_event.real_tag.nsecs = real_nsecs;
        new_event.real_tag.microstep = 0;  //this isn't right. can't be positive that it's 0 for now.
        new_event.actor_to_fire = this_clock->next_actor1;
        new_event.actor_from = this_clock;

        event_insert(new_event);

        if (this_clock->next_actor2 != NULL)
        {
            EVENT new_event2;
            new_event2.actor_to_fire = this_clock->next_actor2;
            event_add(new_event2);
        }

        
        real_secs += 5;
        real_nsecs += 0;
    }

    processEvents();

    return;
}


void computation_fire(ACTOR* this_computation, EVENT* this_event)
{
    //static analysis is used to determine when we can merge in the next level
    //this_tag is modified

/*    EVENT new_event = *this_event;
    
    new_event.this_tag.secs = this_event->this_tag.secs;
    new_event.this_tag.nsecs = this_event->this_tag.nsecs;
    new_event.this_tag.microstep = 0;  //this isn't right. can't be positive that it's 0 for now.
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

    this_event->actor_to_fire = this_computation->next_actor1;
    this_event->actor_from = this_computation;
    
    if (this_computation->next_actor2 != NULL)
    {
        EVENT new_event2 = *this_event;
        new_event2.actor_to_fire = this_computation->next_actor2;
        event_add(new_event2);
    }

    return;
}


void receive_init(ACTOR* this_receiver)
{
    int sock;
    struct sockaddr_in echoserver;
    struct sockaddr_in echoclient;
    char buffer[BUFFSIZE];
    unsigned int echolen, clientlen, serverlen;
    int received = 0;
    int recvint; 
    
    /* Create the UDP socket */
    if ((sock = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0) {
        Die("Failed to create socket");
    }
    /* Construct the server sockaddr_in structure */
    memset(&echoserver, 0, sizeof(echoserver));       /* Clear struct */
    echoserver.sin_family = AF_INET;                  /* Internet/IP */
    echoserver.sin_addr.s_addr = htonl(INADDR_ANY);   /* Any IP address */
    echoserver.sin_port = htons(atoi(GLOB_PORT1));       /* server port */
    //NOTE that the transmitter and receiver make use of port1, port2 is reserved for another
    
    /* Bind the socket */
    serverlen = sizeof(echoserver);
    if (bind(sock, (struct sockaddr *) &echoserver, serverlen) < 0) {
        Die("Failed to bind server socket");
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

        //decode the string into event
        EVENT new_event;
      
        sscanf(buffer, "%d %d %d %d %d %d %d", &new_event.this_value.int_value, &new_event.this_tag.secs, &new_event.this_tag.nsecs, &new_event.this_tag.microstep, &new_event.real_tag.secs, &new_event.real_tag.nsecs, &new_event.real_tag.microstep);
        
        //sscanf(buffer, "%d %d %d %d %d %d %d", new_event.this_value.int_value, new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);
        
        printf("We received these data: %d %.9d %9.9d %3d %.9d %9.9d %d\n", new_event.this_value.int_value, new_event.this_tag.secs, new_event.this_tag.nsecs, new_event.this_tag.microstep, new_event.real_tag.secs, new_event.real_tag.nsecs, new_event.real_tag.microstep);
        
        new_event.actor_to_fire = this_receiver->next_actor1;
        new_event.actor_from = this_receiver;

        event_insert(new_event);

        if (this_receiver->next_actor2 != NULL)
        {
            EVENT new_event2 = new_event;
            new_event.actor_to_fire = this_receiver->next_actor2;
            event_insert(new_event2);
        }
        processEvents();

    } while(1);

    return;
}

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
 
    //*************************************************
    //DEFINE all actors
    //COPY this list to firing_actor function
    
    ACTOR sensor1;
    ACTOR clock1;
    ACTOR transmit1;
    ACTOR receive1;
    ACTOR merge1;
    ACTOR model_delay1;
    ACTOR actuator1;
    ACTOR computation1;

    //CURRENTLY BOTH OF THESE COMMENTED OUT SECTIONS RESULT IN SEGMENTATION FAULT!!!
    //this solves it....

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


    /*
    sensor1.type = "ss1";
    clock1.type = "ck1";
    computation1.type = "cp1";
    transmit1.type = "tr1";
    receive1.type = "rc1";
    model_delay1.type = "md1";
    merge1.type = "mg1";
    actuator1.type = "ac1";
    */

    //Actor_Definition();
    //Get the dependencies of all the actors
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
    
    //Functional_Dependency();
    //initialize the event queue
    //event_init();

    //POSIX semaphores
    //the semaphores are declared as global variables.
    
    int pshared = 0;    //non zero to share between processes

    if (sem_init(&process_sem, pshared, 1) == -1){
        perror("sem_init1");
    }

    if (sem_init(&event_queue_sem, pshared, 1) == -1){
        perror("sem_init2");
    }
    //*******************************************************

    char devFile[23] = "/dev/ptpHwP1000LinuxDr";
    devFile[23] = '\0';
    fd = open(devFile, O_RDWR);
    if (fd < 0)
    {
        printf("Error opening device file \"%s\"\n", devFile);
        exit(1);
    }

    //initialize all actors
    //if a, it's an actuator
    //if s, it's a sensor
    //if neither, error
    if (*argv[4] == 's'){
        sensor_init(&sensor1);
    }
    else if (*argv[4] == 'a'){
        clock_init(&clock1);
        receive_init(&receive1);
    }
    else
        printf("didn't not specify if this is an actuator or sensor!!!\n");


    //go idle
    //while(1){};
    printf("go to sleep\n");
    sleep(99);

    //*******************************************
/*    //remove semaphores
    //THIS CALL TO semctl IS FINE...
    if (semctl(semid, 0, IPC_RMID, 1) == -1) {
        perror("semctl");
        exit(1);
    }
*/    //********************************************
    sem_destroy(&process_sem);
    sem_destroy(&event_queue_sem);
    
    close(fd);

    return 0;
}

//*********************************************************************************
//DEALING WITH THE EVENT QUEUE:
void static_timing_analysis(EVENT* this_event)
{
    printf("Doing static_timing_analysis first\n");
    if (((this_event->actor_to_fire->type[0] == 'm') && (this_event->actor_to_fire->type[1] == 'p') && (this_event->actor_to_fire->type[2] == '1')) && ((this_event->actor_from->type[0] == 'c') && (this_event->actor_from->type[1] == 'p') && (this_event->actor_from->type[2] == '1')))    //THIS IS THE ONLY CASE WHERE THE TIMING NEEDS TO BE RECALCULATED
    {
        printf("we need to do it....\n");
        EVENT new_event = *this_event;

        //let's just say that during static timing analysis, the timing is bounded by 1s for now
        //we take the real_tag, add the total delay through the circuit, then subtract model_dealy
        //we make sure BOUNDED_DELAY - MODEL_DELAY < = 0 for system to be schedulable.
        if (this_event->real_tag.secs >= static_adj_secs)
            new_event.real_tag.secs = this_event->real_tag.secs - static_adj_secs;
        else
            printf("static_adj_secs too large!!!\n");
        if (this_event->real_tag.nsecs >= static_adj_nsecs)
            new_event.real_tag.nsecs= this_event->real_tag.nsecs- static_adj_nsecs;
        else
        {
            new_event.real_tag.nsecs = this_event->real_tag.nsecs + 1000000000 - static_adj_nsecs;
            new_event.real_tag.secs--;
        }

        new_event.real_tag.microstep = 0;  //THIS IS NOT RIGHT
/*          new_event.this_value.value = 0;
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
    }
    else
    {
        //printf("blah\n");
        return;
    }
    return;
}

void event_add(EVENT new_event)
{

    //add an event
    EVENT_LINK *tmp = malloc(sizeof(EVENT_LINK));
    //tmp->next = EVENT_QUEUE_HEAD;
    EVENT_LINK *compare_event = EVENT_QUEUE_HEAD;
    EVENT_LINK *before_event = EVENT_QUEUE_HEAD;
    //move across the link until we reach the microstep phase
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (new_event.this_tag.secs < compare_event->this_event.this_tag.secs)
                stop_flag = 1;
        else if ((new_event.this_tag.secs == compare_event->this_event.this_tag.secs) && (new_event.this_tag.nsecs < compare_event->this_event.this_tag.nsecs))
                stop_flag = 1;
        else if ((new_event.this_tag.secs == compare_event->this_event.this_tag.secs) && (new_event.this_tag.nsecs == compare_event->this_event.this_tag.nsecs) && (new_event.this_tag.microstep < compare_event->this_event.this_tag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            

    tmp->next = compare_event;
    tmp->this_event = new_event;
    if (EVENT_QUEUE_HEAD != NULL)
        before_event->next = tmp;
    else EVENT_QUEUE_HEAD = tmp;

    printf("done adding event\n");
}

void event_insert(EVENT new_event)
{
   
    printf("inserting new event\n");

    if (sem_wait(&event_queue_sem) == -1)
    {
        perror("sem_wait2");
    }
    EVENT_LINK *tmp = malloc(sizeof(EVENT_LINK));
    //tmp->next = EVENT_QUEUE_HEAD;
    EVENT_LINK *compare_event = EVENT_QUEUE_HEAD;
    EVENT_LINK *before_event = EVENT_QUEUE_HEAD;
    //move across the link until we reach the microstep phase
    unsigned int stop_flag = 0;
    while (stop_flag == 0){
        if (compare_event == NULL)
            stop_flag = 1;
        else if (new_event.this_tag.secs < compare_event->this_event.this_tag.secs)
                stop_flag = 1;
        else if ((new_event.this_tag.secs == compare_event->this_event.this_tag.secs) && (new_event.this_tag.nsecs < compare_event->this_event.this_tag.nsecs))
                stop_flag = 1;
        else if ((new_event.this_tag.secs == compare_event->this_event.this_tag.secs) && (new_event.this_tag.nsecs == compare_event->this_event.this_tag.nsecs) && (new_event.this_tag.microstep < compare_event->this_event.this_tag.microstep))
                stop_flag = 1;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            

    tmp->next = compare_event;
    tmp->this_event = new_event;
    if (EVENT_QUEUE_HEAD != NULL)
        before_event->next = tmp;
    else EVENT_QUEUE_HEAD = tmp;

    //UNLOCK
    if (sem_post(&event_queue_sem) == -1) {
        perror("sem_post2");
        exit(1);
    }
    printf("done inserting new event\n");
    //EVENT_QUEUE_OCC = 0;
}

void event_pop()
{
    printf("popping event from queue\n");
    //SEMAPHORE
/*    if (sem_wait(&event_queue_sem) == -1)
    {
        perror("sem_wait2");
    }
*/    //EVENT_QUEUE_OCC = 1;
    if (EVENT_QUEUE_HEAD != NULL){
        EVENT_LINK *event_free = EVENT_QUEUE_HEAD;
        EVENT_QUEUE_HEAD = EVENT_QUEUE_HEAD -> next;
        free(event_free);
    } 
    else printf("event queue is already empty\n");

    //UNLOCK
/*    if (sem_post(&event_queue_sem) == -1) {
        perror("sem_post2");
        exit(1);
    }
*/    
    printf("event poped out of queue\n");
    //EVENT_QUEUE_OCC = 0;
}


