/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 
 Module:       YokePedalSystem.cc
 Authors:      Jean-Baptiste Chaudron & David Saussié
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
INCLUDES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/

#include <iostream>
#include "YokePedalSystem.hh"
#include "Common.hh"

using namespace std;

/*%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
CLASS IMPLEMENTATION
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/


/****************/
/* Constructeur */
/****************/
YokePedalSystem::YokePedalSystem()
{

if (JOYSTICK_ENABLED){
	
    mPedalFd = open("/dev/input/js0", O_RDONLY);
    mYokeFd  = open("/dev/input/js1", O_RDONLY);

    ioctl(mPedalFd, JSIOCGAXES, &mPedalAxisCount);
    ioctl(mYokeFd, JSIOCGAXES, &mYokeAxisCount);
    ioctl(mYokeFd, JSIOCGBUTTONS, &mYokeButtonCount);

    fcntl(mPedalFd, F_SETFL, O_NONBLOCK);
    fcntl(mYokeFd, F_SETFL, O_NONBLOCK);

    for (int i=0;i<mPedalAxisCount;i++) mPedalAxis[i]=0;
    for (int i=0;i<mYokeAxisCount;i++) mYokeAxis[i]=0;
    for (int i=0;i<mYokeButtonCount;i++) mYokeButton[i]=0;

    // Specific Initialization to ensure 0 position throttle at beginning
    mYokeAxis[2] = (int) MAX_INT;
  } else {

    mInputs[0] = 0.01;
    mInputs[1] = 0.01;
    mInputs[2] = 0.01;
    mInputs[3] = 0.01;
    mInputs[4] = 0.01;
    }

    for (int i=0; i<MAX_INPUTS;i++){
        _FailureInputs[i]=0;}

}

/***************/
/* Destructeur */
/***************/
YokePedalSystem::~YokePedalSystem()
{

}

/***************/
/* getInputs() */
/***************/
double* YokePedalSystem::getInputs()
{
if (JOYSTICK_ENABLED) {
    /* Yoke System Acquisition */

    int mYokeResult = read(mYokeFd, &mEvent, sizeof(mEvent));
    if (mYokeResult > 0)
    {
    	switch (mEvent.type)
	{
	    case JS_EVENT_INIT:
	    case JS_EVENT_INIT | JS_EVENT_AXIS:
            mYokeAxis[mEvent.number] = mEvent.value;
            break;

            case JS_EVENT_INIT | JS_EVENT_BUTTON:
            mYokeButton[mEvent.number] = mEvent.value;
            break;

            case JS_EVENT_AXIS:
            mYokeAxis[mEvent.number] = mEvent.value;
            break;
            
            case JS_EVENT_BUTTON:
            mYokeButton[mEvent.number] = mEvent.value;
            break;

            default:
	    cout << "Other event ?" << endl;
            break;
	}
    } 
    else 
    {
        usleep(1);
    }
   
    /* Pedal System Acquisition */
   
    int mPedalResult = read(mPedalFd, &mEvent, sizeof(mEvent));
    if (mPedalResult > 0)
    {
    	switch (mEvent.type)
	{
	    case JS_EVENT_INIT:
	    case JS_EVENT_INIT | JS_EVENT_AXIS:
            break;

            case JS_EVENT_AXIS:
            mPedalAxis[mEvent.number] = mEvent.value;
            break;

            default:
	    cout << "Other event ?" << endl;
            break;
	}
    } 
    else 
    {
        usleep(1);
    }

    /* 0: Aileron  => left (-1) / right (+1) */
    /* 1: Elevator => push (-1) / pull  (+1) */
    /* 2: Rudder   => left (-1) / right (+1) */
    /* 3: Throttle => 0%    (0) / 100%   (1) */
    /* 4: Mode     => Man.  (0) / Autom. (1) */

    if (_FailureInputs[0]==0) {mInputs[0] = (static_cast<double>(mYokeAxis[0]))/( MAX_INT );}
    if (_FailureInputs[1]==0) {mInputs[1] = (static_cast<double>(mYokeAxis[1]))/( MAX_INT );}
    if (_FailureInputs[2]==0) {mInputs[2] = (static_cast<double>(mPedalAxis[2]))/( MAX_INT );}
    if (_FailureInputs[3]==0) {mInputs[3] = (MAX_INT - static_cast<double>(mYokeAxis[2]))/(2 * MAX_INT);}
    if (_FailureInputs[4]==0) {mInputs[4] = (MAX_INT - static_cast<double>(mYokeAxis[3]))/(2 * MAX_INT);}

} 

    return mInputs;
}


/***************/
/* setInputs() */
/***************/
void YokePedalSystem::setInputs(int index,double value){
	if (index>=0 && index<MAX_INPUTS)
		mInputs[index] = value;
}


void YokePedalSystem::setFailureInputs(int index,int value){
	if (index>=0 && index<MAX_INPUTS)
		_FailureInputs[index] = value;
}
