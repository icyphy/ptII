#ifndef TRIGGEREDCLOCK_H
#define TRIGGEREDCLOCK_H

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

// Encode nsec into the HW form
void encodeHwNsec1( FPGA_TIME *hwTime,
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
void decodeHwNsec1(
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



class TriggeredClock : public Actor{
public:
	TriggeredClock(Scheduler* s) : Actor(s), trigger(this), output(this) {}
	virtual void initialize() ;
	virtual void fire() ;
	void setEndTime(Time endTime);
    void setPeriod(Time period);
	void setPhase(Time phase);
	TypedPort<int> trigger, output;
private:
	Time _time;
    Time _endTime;
	Time _period;
	Time _phase;
	Time _startTime;
};
void TriggeredClock::initialize() {
  int fd;
  char *devFile = "/dev/ptpHwP1000LinuxDr";

	_s->registePort(&trigger);
   
  fd = open(devFile, O_RDWR);
  _s->fd = fd;
  if (fd < 0)
     {
         printf("Error opening device file \"%s\"\n", devFile);
         exit(1);
     }
    // Read the current time from the IEEE1588 clock
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
    decodeHwNsec1( &fpgaGetTime.timeVal, &secs, &nsecs);

        _startTime.ms = secs; 
	_startTime.ns = nsecs;

	_endTime.ms += _startTime.ms;

	Token<int> token (1);
	_time.ms = _startTime.ms + _phase.ms;
	_time.ns = _startTime.ns + _phase.ns;
	Event<int> e (token, _time);
	output.send(e);
}

void TriggeredClock::fire() {
	if(trigger.hasToken()) {
		//std::cout <<"fire the Clock actor."<<std::endl;
		
	    trigger.dequeue();
        
		
		_time.ms += _period.ms;
        _time.ns += _period.ns; 
		if (_time.ms < _endTime.ms) {
		    //FIXME: should check whether the currentTime is larger than _time.

			Token<int> token (1);
			Event<int> e2 (token, _time);
			output.send(e2);
		}
	}
}

void TriggeredClock::setEndTime(Time endTime) {
	_endTime = endTime;
}

void TriggeredClock::setPeriod(Time period) {
	_period = period;
}

void TriggeredClock::setPhase(Time phase) {
	_phase = phase;
}

#endif
