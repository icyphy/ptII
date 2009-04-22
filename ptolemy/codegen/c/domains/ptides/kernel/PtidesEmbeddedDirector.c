/*** sharedBlock ***/
typedef struct {
	unsigned long secs;
	unsigned long nsecs;
} Time;

typedef struct {
    Time timestamp;
    int microstep;
} Tag;

struct Actor {
    void (*fire) (void);
    void (*postFire) (void);
}

struct Event {
    union {
        int int_Value; 
        double double_Value;
        long long_Value;
        char char_Value;
    };
    Tag tag;
    void (*fire)(void);
    int channelIndex;
    // we need a port to indicate where this is pointing to.
}

static const int MAX_EVENTS = 100;
Event eventMemory[MAX_EVENTS];
Event* DEADLINE_QUEUE_HEAD = NULL;
static Time currentTime;
static int currentMicrostep;

// FIXME: adding and removing deadlines for events needs to be updated to only
// use the deadline queue but not the event queue.
void addEvent(Event* newEvent) {
// now add event to the deadline queue

	Event *compare_deadline = DEADLINE_QUEUE_HEAD;
	Event *before_deadline  = DEADLINE_QUEUE_HEAD;
	Time tempTime;

	/*Deadline *myNewDeadline = newDeadline();
	myNewDeadline->myEvent = newEvent;
	myNewDeadline->next = NULL;
	myNewDeadline->deadline = newEvent->tag.timestamp + newEvent->atPort->containingActor->deadline;
	  */
	timeAdd(&(newEvent->tag.timestamp), &(newEvent->atPort->containingActor->deadline), &tempTime);
	timeSet(&(newEvent->deadline), &tempTime);
	  
   while (1)
	{
        if (compare_deadline == NULL)
		{
			//RIT128x96x4StringDraw("ce==null",   12,90,15);
            break;
		}

        else if (timeCompare(&(newEvent->deadline), &(compare_deadline->deadline)) <= 0)
		{
		    //RIT128x96x4StringDraw("opt2",   12,90,15);
            break;
		}
      else {
            if (compare_deadline != before_deadline)
		        {
				//RIT128x96x4StringDraw("inifaddedEvent",   10,90,15);
				before_deadline = before_deadline->nextDeadline;
				
				}
			//RIT128x96x4StringDraw("lastelseaddedEvent",   12,90,15);
			
			compare_deadline = compare_deadline->nextDeadline;
			//RIT128x96x4StringDraw("22lastelseaddedEvent",   12,90,15);
		//	break;
        }
    }
            
    newEvent->nextDeadline = compare_deadline;
	//RIT128x96x4StringDraw("check1addedEvent",   12,90,15);
    if (compare_deadline == before_deadline)
	{
        DEADLINE_QUEUE_HEAD = newEvent;
    }
    else if (compare_deadline != before_deadline)
	{
        before_deadline->nextDeadline = newEvent;
    }
    else {
		RIT128x96x4StringDraw("diedinaddedEvent",   0,90,15);
        die("");
    }

	#ifdef LCD_DEBUG
	//sprintf(str,"addedEvent: %d",addeventcount);
	//RIT128x96x4StringDraw(str,   12,80,15);
	#endif

	enableInterrupts();
}

void removeDeadline(Event* thisEvent) {

/*
if removeDeadline removes events from both event queues, it should be ok, but my guess is that it's not exactly doing that for some reason, otherwise I couldn't have gotten that bug.... unless there's another reason for that bug...*/
	Event *current = DEADLINE_QUEUE_HEAD;
	Event *prevDeadline = NULL;
	while (current != thisEvent) {
		prevDeadline = current;
		current = current->nextDeadline;
	}

	 if (current!= NULL)    // should now remove the first from the deadline queue, find it in the event queue and remove its
	{	
		Event * event = current;
		Event *before = EVENT_QUEUE_HEAD;
	    Event *this   = EVENT_QUEUE_HEAD;
		removecount++;
		if(EVENT_QUEUE_HEAD != NULL)
		{
			if(before == event) // delete the first event in the event queue
			{
			EVENT_QUEUE_HEAD = EVENT_QUEUE_HEAD-> nextTag;
			freeEvent(before);
			}
			else
			{
				while(1)
				{
					this = this->nextTag;
					if(this == NULL) {
						die("end of event queue while trying to remove from queue.");
					}				
					if(this == event)
					{
					  	if(this->nextTag == NULL)
					    	before->nextTag = NULL;
					  	else
					  		before->nextTag = this->nextTag;
					  	freeEvent(this);
					  	break;
					}
					before = before->nextTag;
				}
			}
		}//end EVENT_QUEUE_HEAD != NULL

	
	//	DEADLINE_QUEUE_HEAD = DEADLINE_QUEUE_HEAD -> next;
//		if(removecount == addeventcount)
//		 	DEADLINE_QUEUE_HEAD = NULL;
//		else
//			prevDeadline->nextDeadline = current->nextDeadline;

		if (current == DEADLINE_QUEUE_HEAD) {
			DEADLINE_QUEUE_HEAD = current->nextDeadline;
		} else {
			prevDeadline->nextDeadline = current->nextDeadline;				
		}
		
		//freeDeadline(current);                  //I'm not sure if the order this is done in is safe, should technically assign to a temp variable, move head to point to head->next and then free the temp location
        

		//printf("Just removed an event\n");
		#ifdef LCD_DEBUG
		//sprintf(str,"remEventCount %d",removecount);
		//RIT128x96x4StringDraw(str, 0,48,15);
		#endif
    } 
    else die("deadline queue is already empty\n");

}

Event* newEvent(void) {

	while(eventMemory[locationCounter].inUse != MAX_EVENTS+1)
	{  
	   if (locationCounter >= MAX_EVENTS) {
           die("too many events\n"); // if you've run out of memory just stop
       }
	   locationCounter = locationCounter+1;
	}
	locationCounter%=MAX_EVENTS;  // make it circular
	eventMemory[locationCounter].inUse=locationCounter;
	return &eventMemory[locationCounter];
}

void freeEvent(Event * thisEvent) {
	eventMemory[thisEvent->inUse].inUse = MAX_EVENTS+1;
}

/*** time manipulation ***/
void timeAdd(Time* time1, Time* time2, Time* timeSum) {
	timeSum->secs = time1->secs + time2->secs;
	timeSum->nsecs = time1->nsecs + time2->nsecs;
	if (timeSum->nsecs > 1000000000) {
		timeSum->nsecs -= 1000000000;
		timeSum->secs++;
	}
}
int timeCompare(Time* time1, Time* time2) {
	if (time1->secs < time2->secs) {
		return -1;
	} else if (time1->secs == time2->secs && time1->nsecs < time2->nsecs) {
		return -1;
	} else if (time1->secs == time2->secs && time1->nsecs == time2->nsecs) {
		return 0;
	}
	return 1;	
}
void timeSet(Time* time, Time* refTime) {
	time->secs = refTime->secs;
	time->nsecs = refTime->nsecs;
}
void timeSub(Time* time1, Time* time2, Time* timeSub) {
	if (timeCompare(time1, time2) == -1) {
		die("cannot subtract");
	}
	timeSub->secs = time1->secs - time2->secs;
	if (time1->nsecs < time2->nsecs) {
		timeSub->secs--;
		timeSub->nsecs = time1->nsecs + 1000000000 - time2->nsecs;
	} else {
		timeSub->nsecs = time1->nsecs - time2->nsecs;
	}
}
/**/

/**/

/*** preinitPIBlock($director, $name) ***/
// This is the platform independent code
/**/

/*** initPIBlock ***/
initializeEvents();
initPIBlock();
/**/

/*
processEvents() {
    while (1) {
        if (safeToProcess(event)) {
            // do some analysis, and set these values...
            // access to these function ptrs some where?
            event->actor.input1 = true;
            event->actor.input2 = false;

            fire(event);
        }
    }
}

fire(event) {
    if (event->actor == A)
        A_fire();
}

A_fire() {
    //generated
    if (input1 == true) {
        // sourceOutputPort and input are hard coded.
        transferDataFunctionZout1_to_Ain1();
    //    $ref(input) = $ref(sourceOutputPort);
    }
    //FireBlock();
}

transferDataFunctionZout1_to_Ain1(){
    Ain1 = Zout1;
}


// So during codegen, Zout1_to_Ain1 would be generated using:
// $ref(input), $ref(sourceOutputPort)
*/
