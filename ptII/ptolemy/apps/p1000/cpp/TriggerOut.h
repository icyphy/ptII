#ifndef TRIGGEROUT_H
#define TRIGGEROUT_H

#include <stdio.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <math.h>

#include "actor.h"
//#include "port.h"


#include "ptpHwP1000LinuxDr.h"

int fd;

// Encode nsec into the HW form
void encodeHwNsec2( FPGA_TIME *hwTime,
        const unsigned int secs,
        const unsigned int nsec)
{
    static double nsecToHw = pow(2,30) / 1e9;

    if (hwTime)
        {
            hwTime->hwNsec = (unsigned int) ( nsec * nsecToHw); // 0.93132257461548);
            hwTime->secs = secs;
        }
}

// Decode sec, nsec from the HW form. Deal with even and odd values
void decodeHwNsec2(
        const FPGA_TIME *hwTime,
        unsigned int *secs,
        unsigned int *nsec)
{
    static double hwToNsec = 1e9 / pow(2,30);

    if (hwTime)
        {

            *nsec = (unsigned int) ( (hwTime->hwNsec & 0x7fffffff) * hwToNsec); //1.073741824);
    *secs = hwTime->secs;
    if ( hwTime->hwNsec & 0x80000000)
        {
            // Must add 10 nsec
            *nsec += 10;
            if (*nsec >= 1000000000)
                {
                    *secs = *secs + 1;
                    *nsec = *nsec - 1000000000;
                }

        }
	}

}

void* read_loop2(void* data)
{
  unsigned int secs;
  unsigned int nsecs;
  int rtn;
  Time t;
  char* data1 = "X";

  do {

    // Block until the next interrupt
    unsigned int status;
    do
      {
         int num = read( fd, &status, sizeof(status));
         if (num != sizeof( status))
             {
                 fprintf(stderr, "Error reading status, %d\n", num);
                 exit(1);
             }
      } while ((status & TIMEBOMB_0_FIRE) == 0); // Got it!
    //      } while ((status & TIMESTAMP_0_RCV) == 0); // Got it!


    FPGA_GET_TIME fpgaGetTime;
    int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
      {
         fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
	 //         exit(1);
	 //	 continue;
      }

    // Scale from HW to TAI nsec
    unsigned int secs;
    unsigned int nsecs;
    decodeHwNsec2( &fpgaGetTime.timeVal, &secs, &nsecs);
    printf("  sw TO: %.9d.%9.9d\n", secs, nsecs);
    //    printf("\n%s>\n", (char *)data1);

    /*
    // Read all available timestamps
    FPGA_GET_TIMESTAMP fpgaGetTimestamp;
    int lastSeqNum = -1;
    do
      {
         // Read a timestamp from the log
         rtn = ioctl(fd,  FPGA_IOC_GET_TIMESTAMP, &fpgaGetTimestamp);
         if (rtn)
             {
                 fprintf(stderr, "ioctl to get timestamp failed: %d, %d\n", rtn, errno);
                 perror("error from ioctl");
                 exit(1);
             }

         // Stop when empty
         if (fpgaGetTimestamp.seqNum == 0 &&
	     fpgaGetTimestamp.timeVal.secs == 0 &&
             fpgaGetTimestamp.timeVal.hwNsec == 0) break; // done

         // Decode
         decodeHwNsec2( &fpgaGetTimestamp.timeVal, &secs, &nsecs);
	 printf("\n  Trig Out on %s: %.9d.%9.9d\n", (char *)data1, secs, nsecs);
	 //	 printf("\n  Trig IN on %s: %.9d.%9.9d\n", (char *)data1, secs, nsecs);
	 //         printf("Timestamp: %4d %.9d.%9.9d\n", fpgaGetTimestamp.seqNum, secs, nsecs);

         lastSeqNum = fpgaGetTimestamp.seqNum;

      } while (1);


    // Clear the timestamp log
    FPGA_CLEAR_TIMESTAMP fpgaClearTimestamp; // no data needed at this time;
    rtn = ioctl(fd,  FPGA_IOC_CLEAR_TIMESTAMP, &fpgaClearTimestamp);
    if (rtn)
      {
         fprintf(stderr, "ioctl to clear timestamp log failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
         exit(1);
      }

    */
		//For test purpose: 
	        t.ms = secs; 
		t.ns = nsecs;
		Token<int> token (1);
		Event<int> e2 (token, t);
	        ((TypedPort<int>*) data)->send(e2);
 

  } while (1);

  pthread_exit(NULL);
}


class TriggerOut : public Actor{
public:
	TriggerOut(Scheduler* s) : Actor(s), input(this), output(this) {}
	virtual void initialize() ;
	virtual void fire() ;
	TypedPort<int> input, output;

};

void TriggerOut::initialize() {
	_s->registePort(&input);

	//FIXME: create thread here...
	int        thr_id;
	pthread_t  p_thread;
	char *hostName = "X";
        thr_id = pthread_create(&p_thread, NULL, read_loop2, (void*)&output);
}


void TriggerOut::fire() {
    unsigned int secs;
    unsigned int nsecs;

    fd = _s->fd;

	if(input.hasToken()) {


    FPGA_GET_TIME fpgaGetTime;
    int rtn = ioctl(fd,  FPGA_IOC_GET_TIME, &fpgaGetTime);
    if (rtn)
      {
         fprintf(stderr, "ioctl to get time failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
	 //         exit(1);
	 //	 continue;
      }

    // Scale from HW to TAI nsec
    decodeHwNsec2( &fpgaGetTime.timeVal, &secs, &nsecs);
    printf("\n set TO: %.9d.%9.9d\n", secs, nsecs);

		//std::cout <<"fire the Clock actor."<<std::endl;
	    Event<int>* e;
	    e = input.dequeue();
	    //e->printContent();
		Time t = e->getTime();
		
        //FIXME: set hardware trigger time.

		secs = t.ms;
		nsecs = t.ns;
	 printf("     TO: %.9d.%9.9d\n", secs, nsecs);

    FPGA_SET_TIMETRIGGER fpgaSetTimetrigger;
    fpgaSetTimetrigger.num = 0;   // Only single timetrigger supported, numbered '0'
    fpgaSetTimetrigger.force = 0; // Don't force
    encodeHwNsec2( &fpgaSetTimetrigger.timeVal, secs, nsecs);

        rtn = ioctl(fd,  FPGA_IOC_SET_TIMETRIGGER, &fpgaSetTimetrigger);
    if (rtn)
      {
         fprintf(stderr, "ioctl to set timetrigger failed: %d, %d\n", rtn, errno);
         perror("error from ioctl");
	 //         exit(1);
	 //	 continue;
      }
        

	}
}

#endif
