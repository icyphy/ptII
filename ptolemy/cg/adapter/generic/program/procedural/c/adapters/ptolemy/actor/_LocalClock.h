/* In this file we have defined a struct LocalClock
 * which deals with the notion of time in the generated directors
 *
 * @author : William Lucas
 */

#ifndef LOCALCLOCK_H_
#define LOCALCLOCK_H_

#include <stdlib.h>
#include <stdbool.h>

#include "_ptTypes.h"
#include "_Director.h"

struct LocalClock {
    struct Director* container;

    Time _localTime;
    double _drift;
    Time _lastCommitEnvironmentTime;
    Time _lastCommitLocalTime;
    Time _offset;
    double _timeResolution;

    void (*free)(struct LocalClock*);

    double (*getClockDrift)(struct LocalClock*);
    Time (*getEnvironmentTimeForLocalTime)(struct LocalClock*, Time);
    Time (*getLocalTime)(struct LocalClock*);
    Time (*getLocalTimeForCurrentEnvironmentTime)(struct LocalClock*);
    Time (*getLocalTimeForEnvironmentTime)(struct LocalClock*, Time);
    double (*getTimeResolution)(struct LocalClock*);
    void (*initialize)(struct LocalClock*);
    void (*resetLocalTime)(struct LocalClock*, Time);
    void (*setClockDrift)(struct LocalClock*, double);
    void (*setLocalTime)(struct LocalClock*, Time);
    void (*setTimeResolution)(struct LocalClock*, double);
    void (*start)(struct LocalClock*);
    void (*stop)(struct LocalClock*);
};

struct LocalClock* LocalClock_New();
void LocalClock_Init(struct LocalClock* clock);
void LocalClock_New_Free(struct LocalClock* clock);

double LocalClock_GetClockDrift(struct LocalClock* clock);
Time LocalClock_GetEnvironmentTimeForLocalTime(struct LocalClock* clock, Time time);
Time LocalClock_GetLocalTime(struct LocalClock* clock);
Time LocalClock_GetLocalTimeForCurrentEnvironmentTime(struct LocalClock* clock);
Time LocalClock_GetLocalTimeForEnvironmentTime(struct LocalClock* clock, Time time);
double LocalClock_GetTimeResolution(struct LocalClock* clock);
void LocalClock_Initialize(struct LocalClock* clock);
void LocalClock_ResetLocalTime(struct LocalClock* clock, Time time);
void LocalClock_SetClockDrift(struct LocalClock* clock, double drift);
void LocalClock_SetLocalTime(struct LocalClock* clock, Time time);
void LocalClock_SetTimeResolution(struct LocalClock* clock, double timeResolution);
void LocalClock_Start(struct LocalClock* clock);
void LocalClock_Stop(struct LocalClock* clock);

#endif
