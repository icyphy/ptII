/* Main program of the scheduler demo with some actor definitions.

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

Clock_TypeData Clock_typeData = {
	/* GeneralType fields. */
	&Actor_typeData,			// superType
	"Clock",					// typeName
	sizeof(Clock),				// size
	
	/* Actor fields. */
	Clock_fire					// fire
	
	/* Clock fields. */
};

/**
 * Initiate a clock, and assign a scheduler to it.
 * 
 * @param clock The clock to be initiated.
 * @param actual_type_data The type data of the int clock's actual type, or
 *  NULL. When NULL is given (which is usually the case when called by the
 *  user), Clock_typeData is used.
 * @param scheduler The scheduler.
 */
void Clock_init(Clock* clock, Clock_TypeData* actual_type_data,
	Scheduler* scheduler) {
	
	Actor_init((Actor*) clock, (Actor_TypeData*) (actual_type_data == NULL ?
			&Clock_typeData : actual_type_data), scheduler);

	TypedPort_init(&(clock->fireAt), NULL, (Actor*) clock);
	TypedPort_init(&(clock->output), NULL, (Actor*) clock);
	clock->time = clock->endTime = (Time) {
		0,	// ms
		0,	// ns
	};
}

/**
 * Fire the Clock.
 * 
 * @param clock The clock to be fired.
 */
void Clock_fire(Actor* actor) {
	Actor_fire(actor);
}

TriggeredClock_TypeData TriggeredClock_typeData = {
	/* GeneralType fields. */
	&Clock_typeData,			// superType
	"TriggeredClock",			// typeName
	sizeof(TriggeredClock),		// size
	
	/* Actor fields. */
	TriggeredClock_fire			// fire
	
	/* Clock fields. */
	
	/* TriggeredClock fields. */
};

/**
 * Initiate a triggered clock, and assign a scheduler to it.
 * 
 * @param triggered_clock The triggered clock to be initiated.
 * @param actual_type_data The type data of the int triggered clock's actual
 *  type, or NULL. When NULL is given (which is usually the case when called by
 *  the user), TriggeredClock_typeData is used.
 * @param scheduler The scheduler.
 */
void TriggeredClock_init(TriggeredClock* triggered_clock,
	TriggeredClock_TypeData* actual_type_data, Scheduler* scheduler) {
	
	Clock_init((Clock*) triggered_clock,
		(Clock_TypeData*) (actual_type_data == NULL ?
				&TriggeredClock_typeData : actual_type_data), scheduler);
	
	TypedPort_init(&(triggered_clock->trigger), NULL, (Actor*)triggered_clock);
	TypedPort_init(&(triggered_clock->output), NULL, (Actor*)triggered_clock);
	triggered_clock->startTime = triggered_clock->phase
			= triggered_clock->period = (Time) {
		0,	// ms
		0	// ns
	};
}

/**
 * Initialize the triggered clock. This method should be called before the
 * execution starts.
 * 
 * @param triggered_clock The triggered clock.
 */
void TriggeredClock_initialize(TriggeredClock* triggered_clock) {
	int fd;
	char *devFile = "/dev/ptpHwP1000LinuxDr";
	Scheduler* scheduler;
    FPGA_GET_TIME fpgaGetTime;
    int rtn;
    unsigned int secs;
    unsigned int nsecs;
	IntToken token;
	Event e;
	
	scheduler = ((Actor*)triggered_clock)->scheduler;
	Scheduler_registerPort(scheduler, (Port*) &(triggered_clock->trigger));
		
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

	triggered_clock->startTime = (Time) { secs, nsecs };
	((Clock*)triggered_clock)->endTime.ms += triggered_clock->startTime.ms;

	IntToken_init(&token, NULL);
	token.value = 1;
	
	((Clock*)triggered_clock)->time = (Time) {
		triggered_clock->startTime.ms + triggered_clock->phase.ms,
		triggered_clock->startTime.ns + triggered_clock->phase.ns
	};
	e = (Event) {
		(Token*) &token,					// token
		((Clock*) triggered_clock)->time,	// time
		0									// isTimerEvent
	};
	TypedPort_send(&(triggered_clock->output), &e);
}

/**
 * Fire the triggered clock.
 * 
 * @param triggered_clock The triggered clock to be fired.
 */
void TriggeredClock_fire(Actor* actor) {
	TriggeredClock* triggered_clock = (TriggeredClock*) actor;
	Clock* clock = (Clock*) triggered_clock;
	IntToken token;
	Event out_e, *in_e;
	
	Clock_fire(actor);
	
	in_e = BidirList_removeFirst(&(triggered_clock->trigger.eventQueue));
	if (in_e != NULL) {
		clock->time.ms += triggered_clock->period.ms;
		clock->time.ns += triggered_clock->period.ns; 
		if (clock->time.ms < clock->endTime.ms) {
			//FIXME: should check whether the currentTime is larger than _time.
			IntToken_init(&token, NULL);
			token.value = 1;
			
			out_e = (Event) {
				(Token*) &token,		// token
				clock->time,			// time
				0,						// isTimerEvent
			};
			TypedPort_send(&(triggered_clock->output), &out_e);
		}
	}
}

TriggerOut_TypeData TriggerOut_typeData = {
	/* GeneralType fields. */
	&Clock_typeData,			// superType
	"TriggerOut",				// typeName
	sizeof(TriggerOut),			// size
	
	/* Actor fields. */
	TriggerOut_fire				// fire
	
	/* Clock fields. */
	
	/* TriggerOut fields. */
};

/**
 * Initiate a trigger out actor, and assign a scheduler to it.
 * 
 * @param trigger_out The trigger out actor to be initiated.
 * @param actual_type_data The type data of the triggered out actor 's actual
 *  type, or NULL. When NULL is given (which is usually the case when called by
 *  the user), TriggerOut_typeData is used.
 * @param scheduler The scheduler.
 */
void TriggerOut_init(TriggerOut* trigger_out,
	TriggerOut_TypeData* actual_type_data, Scheduler* scheduler) {
	
	Clock_init((Clock*) trigger_out,
		(Clock_TypeData*) (actual_type_data == NULL ?
				&TriggerOut_typeData : actual_type_data), scheduler);
	
	TypedPort_init(&(trigger_out->input), NULL, (Actor*) trigger_out);
	TypedPort_init(&(trigger_out->output), NULL, (Actor*) trigger_out);
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
	Time t;
	unsigned int status;
	int num;
	Event e;
	IntToken token;

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
		rtn = ioctl(fd, FPGA_IOC_GET_TIME, &fpgaGetTime);
		if (rtn) {
			fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
			exit(1);
		}

		// Scale from HW to TAI nsec
		decodeHwNsec(&fpgaGetTime.timeVal, &secs, &nsecs);
		printf("  sw TO: %.9d.%9.9d\n", secs, nsecs);
		//printf("\n%s>\n", (char *)data1);
		
		t.ms = secs; 
		t.ns = nsecs;
		IntToken_init(&token, NULL);
		token.value = 1;
		e = (Event) {
			(Token*) &token,				// token
			t,								// time
			0,								// is_timer_event
		};
		TypedPort_send((TypedPort*) data, &e);
	} while (1);

	pthread_exit(NULL);
	
	return NULL;
}

/**
 * Initialize the trigger out actor. This method should be called before the
 * execution starts.
 * 
 * @param trigger_out The trigger out actor.
 */
void TriggerOut_initialize(TriggerOut* trigger_out) {
	int        thr_id;
	pthread_t  p_thread;
	Scheduler* scheduler;
	
	scheduler = ((Actor*) trigger_out)->scheduler;
	Scheduler_registerPort(scheduler, (Port*) &(trigger_out->input));
	
	//FIXME: create thread here...
	thr_id = pthread_create(&p_thread, NULL, read_loop, &(trigger_out->output));
}

/**
 * Fire the trigger out actor.
 * 
 * @param trigger_out The trigger out actor to be fired.
 */
void TriggerOut_fire(Actor* actor) {
	TriggerOut* trigger_out = (TriggerOut*) actor;
	unsigned int secs;
	unsigned int nsecs;
    FPGA_GET_TIME fpgaGetTime;
    int rtn;
	FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
	Event* in_e;
	Time t;

	Clock_fire(actor);
	
	fd = ((Actor*) trigger_out)->scheduler->fd;
	in_e = BidirList_removeFirst(&(trigger_out->input.eventQueue));
	if (in_e != NULL) {
		rtn = ioctl(fd, FPGA_IOC_GET_TIME, &fpgaGetTime);
		if (rtn) {
			fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
			perror("error from ioctl");
			//exit(1);
		}

		// Scale from HW to TAI nsec
		decodeHwNsec(&fpgaGetTime.timeVal, &secs, &nsecs);
		printf("\n set TO: %.9d.%9.9d\n", secs, nsecs);

		//FIXME: set hardware trigger time.
		t = in_e->time;
		secs = t.ms;
		nsecs = t.ns;
		printf("     TO: %.9d.%9.9d\n", secs, nsecs);

		// Only single timetrigger supported, numbered '0'.
		fpgaSetTimetrigger.num = 0;
		// Don't force;
		fpgaSetTimetrigger.force = 0;
		encodeHwNsec(&fpgaSetTimetrigger.timeVal, secs, nsecs);

		rtn = ioctl(fd, FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
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
	Scheduler scheduler;	
	TriggeredClock t_clock;
	TriggerOut t_out;

	Scheduler_init(&scheduler, NULL);
	TriggeredClock_init(&t_clock, NULL, &scheduler);
	TriggerOut_init(&t_out, NULL, &scheduler);

	Port_connect((Port*) &(t_clock.output), (Port*) &(t_out.input));
	Port_connect((Port*) &(t_out.output), (Port*) &(t_clock.trigger));
	
	((Clock*) &t_clock)->endTime = (Time) {50, 0};
	t_clock.period = (Time) {5, 0};
	t_clock.phase = (Time) {1, 0};
	TriggeredClock_initialize(&t_clock);
	TriggerOut_initialize(&t_out);
	
	printf("Start execution:\n");
	Scheduler_execute(&scheduler);
}
