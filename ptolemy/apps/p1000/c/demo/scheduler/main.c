/* Main program of the scheduler demo with some actor definitions.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 @author Thomas Huining Feng, Yang Zhao

 */

#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <pthread.h>

#include "main.h"
#include "int_token.h"
#include "ptpHwP1000LinuxDr.h"
#include "p1000_utils.h"

/**
 * Constant for CLOCK type's method table.
 */
CLOCK_METHOD_TABLE CLOCK_method_table = {
	CLOCK_fire	// fire
};

/**
 * Initiate an object of the CLOCK type, and assign a scheduler to it.
 * 
 * @param clock Reference to the CLOCK object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void CLOCK_init(CLOCK* clock, void* actual_ref, SCHEDULER* scheduler) {
	INIT_SUPER_TYPE(CLOCK, ACTOR, clock, actual_ref, &CLOCK_method_table,
		scheduler);

	ACTOR* ACTOR_super = UPCAST(clock, ACTOR);
	TYPED_PORT_init(&(clock->fire_at), &(clock->fire_at), ACTOR_super);
	TYPED_PORT_init(&(clock->output), &(clock->output), ACTOR_super);
	clock->time = clock->end_time = (TIME) {
		0,	// ms
		0,	// ns
	};
}

/**
 * Fire the CLOCK.
 * 
 * @param actor Reference to the CLOCK object.
 */
void CLOCK_fire(CLOCK* clock) {
	ACTOR_fire((ACTOR*) SUPER(clock));
}

/**
 * Constant for TRIGGERED_CLOCK's method table.
 */
TRIGGERED_CLOCK_METHOD_TABLE TRIGGERED_CLOCK_method_table = {
	TRIGGERED_CLOCK_fire	// fire
};

/**
 * Initiate an object of the TRIGGERED_CLOCK type, and assign a scheduler to it.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object to be
 *  initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void TRIGGERED_CLOCK_init(TRIGGERED_CLOCK* triggered_clock, void* actual_ref,
	SCHEDULER* scheduler) {
	ACTOR* ACTOR_super;
	
	INIT_SUPER_TYPE(TRIGGERED_CLOCK, CLOCK, triggered_clock, actual_ref,
		&TRIGGERED_CLOCK_method_table, scheduler);
	
	ACTOR_super = UPCAST(triggered_clock, ACTOR);
	TYPED_PORT_init(&(triggered_clock->trigger), &(triggered_clock->trigger),
		ACTOR_super);
	TYPED_PORT_init(&(triggered_clock->output), &(triggered_clock->output),
		ACTOR_super);
	triggered_clock->start_time = triggered_clock->phase
			= triggered_clock->period = (TIME) {
		0,	// ms
		0	// ns
	};
}

/**
 * Initialize the TRIGGERED_CLOCK object. This method is called before the
 * execution starts.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object.
 */
void TRIGGERED_CLOCK_initialize(TRIGGERED_CLOCK* triggered_clock) {
	int fd;
	char *devFile = "/dev/ptpHwP1000LinuxDr";
	SCHEDULER* scheduler;
    FPGA_GET_TIME fpgaGetTime;
    int rtn;
    unsigned int secs;
    unsigned int nsecs;
	INT_TOKEN token;
	CLOCK* triggered_clock_CLOCK;
	EVENT e;
	
	scheduler = UPCAST(triggered_clock, ACTOR)->scheduler;
	SCHEDULER_register_port(scheduler,
		UPCAST(&(triggered_clock->trigger), PORT));
		
	fd = open(devFile, O_RDWR);
	if (fd < 0) {
		fprintf(stderr, "Error opening device file \"%s\"\n", devFile);
		//exit(1);
	}
	scheduler->fd = fd;
	
    // Read the current time from the IEEE1588 clock
    rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn) {
		fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
		//exit(1);
	}

    // Scale from HW to TAI nsec
    decodeHwNsec(&fpgaGetTime.timeVal, &secs, &nsecs);

	triggered_clock->start_time = (TIME) { secs, nsecs };
	UPCAST(triggered_clock, CLOCK)->end_time.ms +=
			triggered_clock->start_time.ms;

	INT_TOKEN_init(&token, &token);
	token.value = 1;
	
	triggered_clock_CLOCK = UPCAST(triggered_clock, CLOCK);
	triggered_clock_CLOCK->time = (TIME) {
		triggered_clock->start_time.ms + triggered_clock->phase.ms,
		triggered_clock->start_time.ns + triggered_clock->phase.ns
	};
	e = (EVENT) {
		UPCAST(&token, TOKEN),			// token
		triggered_clock_CLOCK->time,	// time
		0,								// is_timer_event
		NULL,							// prev
		NULL							// next
	};
	TYPED_PORT_send(&(triggered_clock->output), &e);
}

/**
 * Fire the TRIGGERED_CLOCK.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object.
 */
void TRIGGERED_CLOCK_fire(TRIGGERED_CLOCK* triggered_clock) {
	CLOCK* triggered_clock_CLOCK;
	INT_TOKEN token;
	EVENT out_e, *in_e;
	
	CLOCK_fire((CLOCK*) SUPER(triggered_clock));
	
	if (triggered_clock->trigger.first_event != NULL) {
		in_e = triggered_clock->trigger.first_event;
		REMOVE_FIRST(triggered_clock->trigger.first_event,
			triggered_clock->trigger.last_event);
		free(in_e);
		
		triggered_clock_CLOCK = UPCAST(triggered_clock, CLOCK);
		triggered_clock_CLOCK->time.ms += triggered_clock->period.ms;
		triggered_clock_CLOCK->time.ns += triggered_clock->period.ns; 
		if (triggered_clock_CLOCK->time.ms
				< triggered_clock_CLOCK->end_time.ms) {
			//FIXME: should check whether the currentTime is larger than _time.
			INT_TOKEN_init(&token, &token);
			token.value = 1;
			
			out_e = (EVENT) {
				UPCAST(&token, TOKEN),			// token
				triggered_clock_CLOCK->time,	// time
				0,								// is_timer_event
				NULL,							// prev
				NULL							// next
			};
			TYPED_PORT_send(&(triggered_clock->output), &out_e);
		}
	}
}

/**
 * Constant for TRIGGER_OUT's method table.
 */
TRIGGER_OUT_METHOD_TABLE TRIGGER_OUT_method_table = {
	TRIGGER_OUT_fire	// fire
};

/**
 * Initiate an object of the TRIGGER_OUT type, and assign a scheduler to it.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void TRIGGER_OUT_init(TRIGGER_OUT* trigger_out, void* actual_ref,
	SCHEDULER* scheduler) {
	ACTOR* ACTOR_super;
	
	INIT_SUPER_TYPE(TRIGGER_OUT, ACTOR, trigger_out, actual_ref,
		&TRIGGER_OUT_method_table, scheduler);
	
	ACTOR_super = UPCAST(trigger_out, ACTOR);
	TYPED_PORT_init(&(trigger_out->input), &(trigger_out->input), ACTOR_super);
	TYPED_PORT_init(&(trigger_out->output), &(trigger_out->output),
		ACTOR_super);
}

/* FIXME: File descriptor used in read_loop. */
int fd;

/**
 * Loop infinitely in a separate thread to pull data from the P1000 device. When
 * a trigger signal is received from the device, the current time is immediately
 * retrieved and sent to the port, specified by the data parameter. This data,
 * as a token encapsulated in an event, is passed in the heap to the responding
 * actor.
 * 
 * @param data Reference to the port where the timed hardware signal should be
 *  sent. Though data is declared to be voi*, its actual type must be
 *  TYPED_PORT.
 * @return NULL.
 */
void* read_loop(void* data) {
	unsigned int secs;
	unsigned int nsecs;
	int rtn;
	TIME t;
	unsigned int status;
	int num;
	EVENT e;
	INT_TOKEN token;

	do {
		// Block until the next interrupt
		do {
			num = read(fd, &status, sizeof(status));
			if (num != sizeof( status)) {
				fprintf(stderr, "Error reading status, %d\n", num);
				exit(1);
			}
		} while ((status & TIMEBOMB_0_FIRE) == 0); // Got it!
		//} while ((status & TIMESTAMP_0_RCV) == 0); // Got it!

		FPGA_GET_TIME fpgaGetTime;
		rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
		if (rtn) {
			fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
			exit(1);
		}

		// Scale from HW to TAI nsec
		decodeHwNsec( &fpgaGetTime.timeVal, &secs, &nsecs);
		printf("  sw TO: %.9d.%9.9d\n", secs, nsecs);
		//printf("\n%s>\n", (char *)data1);
		
		t.ms = secs; 
		t.ns = nsecs;
		INT_TOKEN_init(&token, &token);
		token.value = 1;
		e = (EVENT) {
			UPCAST(&token, TOKEN),			// token
			t,								// time
			0,								// is_timer_event
			NULL,							// prev
			NULL							// next
		};
		TYPED_PORT_send((TYPED_PORT*) data, &e);
	} while (1);

	pthread_exit(NULL);
	
	return NULL;
}

/**
 * Initialize the TRIGGER_OUT object. This method is called before the execution
 * starts.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object.
 */
void TRIGGER_OUT_initialize(TRIGGER_OUT* trigger_out) {
	int        thr_id;
	pthread_t  p_thread;
	SCHEDULER* scheduler;
	
	scheduler = UPCAST(trigger_out, ACTOR)->scheduler;
	SCHEDULER_register_port(scheduler, UPCAST(&(trigger_out->input), PORT));
	
	//FIXME: create thread here...
	thr_id = pthread_create(&p_thread, NULL, read_loop,
		UPCAST(&(trigger_out->output), PORT));
}

/**
 * Fire the TRIGGER_OUT.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object.
 */
void TRIGGER_OUT_fire(TRIGGER_OUT* trigger_out) {
	unsigned int secs;
	unsigned int nsecs;
    FPGA_GET_TIME fpgaGetTime;
    int rtn;
	FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
	EVENT* in_e;
	TIME t;

	CLOCK_fire((CLOCK*) SUPER(trigger_out));
	
	fd = UPCAST(trigger_out, ACTOR)->scheduler->fd;
	if (trigger_out->input.first_event != NULL) {
		rtn = ioctl(fd, FPGA_IOC_GET_TIME, &fpgaGetTime);
		if (rtn) {
			fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
			perror("error from ioctl");
			//exit(1);
		}

		// Scale from HW to TAI nsec
		decodeHwNsec(&fpgaGetTime.timeVal, &secs, &nsecs);
		printf("\n set TO: %.9d.%9.9d\n", secs, nsecs);

		in_e = trigger_out->input.first_event;
		t = in_e->time;
		REMOVE_FIRST(trigger_out->input.first_event,
			trigger_out->input.last_event);
		free(in_e);

		//FIXME: set hardware trigger time.
		secs = t.ms;
		nsecs = t.ns;
		printf("     TO: %.9d.%9.9d\n", secs, nsecs);

		// Only single timetrigger supported, numbered '0'.
		fpgaSetTimetrigger.num = 0;
		// Don't force;
		fpgaSetTimetrigger.force = 0;
		encodeHwNsec(&fpgaSetTimetrigger.timeVal, secs, nsecs);

		rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
		if (rtn) {
			fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn,
				errno);
			perror("error from ioctl");
			// exit(1);
		}
	}
}

/**
 * The main function of the scheduler demo. It creates a TRIGGERED_CLOCK actor
 * and a TRIGGER_OUT actor in a feedback loop, and execute the system with a
 * discrete event scheduler..
 */
int main() {
	SCHEDULER scheduler;	
	TRIGGERED_CLOCK t_clock;
	TRIGGER_OUT t_out;

	SCHEDULER_init(&scheduler, &scheduler);
	TRIGGERED_CLOCK_init(&t_clock, &t_clock, &scheduler);
	TRIGGER_OUT_init(&t_out, &t_out, &scheduler);

	PORT_connect(UPCAST(&(t_clock.output), PORT), UPCAST(&(t_out.input), PORT));
	PORT_connect(UPCAST(&(t_out.output), PORT),
		UPCAST(&(t_clock.trigger), PORT));
	
	UPCAST(&t_clock, CLOCK)->end_time = (TIME) {50, 0};
	t_clock.period = (TIME) {5, 0};
	t_clock.phase = (TIME) {1, 0};
	TRIGGERED_CLOCK_initialize(&t_clock);
	TRIGGER_OUT_initialize(&t_out);
	
	printf("Start execution:\n");
	SCHEDULER_execute(&scheduler);
}
