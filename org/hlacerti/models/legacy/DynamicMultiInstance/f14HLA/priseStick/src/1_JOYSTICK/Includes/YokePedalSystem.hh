/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
 Module:       YokePedalSystem.hh
 Authors:      Jean-Baptiste Chaudron & David Saussié
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
SENTRY
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#include <iostream>

#ifndef YOKE_PEDAL_SYSTEM_HH
#define YOKE_PEDAL_SYSTEM_HH

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include <linux/joystick.h>


const int MAX_PEDAL_AXIS = 3;
const int MAX_YOKE_AXIS = 7;
const int MAX_YOKE_BUTTON = 23;
const int MAX_INPUTS = 5;
const double MAX_INT = 32767;

using namespace std;

class YokePedalSystem
{
public:
	
    YokePedalSystem();
    virtual ~YokePedalSystem();
    double* getInputs();
    void setInputs(int index,double value);
    void setFailureInputs(int index,int value);

    // Failure mode
    int _FailureInputs[MAX_INPUTS];

private:

    unsigned char mPedalAxisCount;
    unsigned char mYokeAxisCount;
    unsigned char mYokeButtonCount;
    int mYokeFd;
    int mPedalFd;
	
    int mPedalAxis[MAX_PEDAL_AXIS];
    int mYokeAxis[MAX_YOKE_AXIS];
    int mYokeButton[MAX_YOKE_BUTTON];
    double mInputs[MAX_INPUTS];

    int mYokeResult;
    int mPedalResult;

    js_event mEvent;


};
#endif
