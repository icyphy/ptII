#include "_LocalClock.h"

void _commit(struct LocalClock* clock);

struct LocalClock* LocalClock_New() {
    struct LocalClock* newLocalClock = calloc(1, sizeof(struct LocalClock));
    if (newLocalClock == NULL) {
        fprintf(stderr, "Allocation error : LocalClock_New\n");
        exit(-1);
    }
    LocalClock_Init(newLocalClock);
    newLocalClock->free = LocalClock_New_Free;

    return newLocalClock;
}
void LocalClock_Init(struct LocalClock* clock) {
    clock->container = NULL;
    clock->_localTime = -DBL_MAX;
    clock->_drift = 1.0;
    clock->_lastCommitEnvironmentTime = -DBL_MAX;
    clock->_lastCommitLocalTime = -DBL_MAX;
    clock->_offset = -DBL_MAX;
    clock->_timeResolution = 1E-10;

    clock->getClockDrift = LocalClock_GetClockDrift;
    clock->getEnvironmentTimeForLocalTime = LocalClock_GetEnvironmentTimeForLocalTime;
    clock->getLocalTime = LocalClock_GetLocalTime;
    clock->getLocalTimeForCurrentEnvironmentTime = LocalClock_GetLocalTimeForCurrentEnvironmentTime;
    clock->getLocalTimeForEnvironmentTime = LocalClock_GetLocalTimeForEnvironmentTime;
    clock->getTimeResolution = LocalClock_GetTimeResolution;
    clock->initialize = LocalClock_Initialize;
    clock->resetLocalTime = LocalClock_ResetLocalTime;
    clock->setClockDrift = LocalClock_SetClockDrift;
    clock->setLocalTime = LocalClock_SetLocalTime;
    clock->setTimeResolution = LocalClock_SetTimeResolution;
    clock->start = LocalClock_Start;
    clock->stop = LocalClock_Stop;
}
void LocalClock_New_Free(struct LocalClock* clock) {
    if (clock)
        free(clock);
}

double LocalClock_GetClockDrift(struct LocalClock* clock) {
    return clock->_drift;
}
Time LocalClock_GetEnvironmentTimeForLocalTime(struct LocalClock* clock, Time time) {
    if (time < clock->_lastCommitLocalTime) {
        fprintf(stderr, "Cannot compute environment time for local time %lf\
                                 because the last commit of the local time occurred at \
                                 local time %lf : LocalClock_GetEnvironmentTimeForLocalTime\n",
                time, clock->_lastCommitLocalTime);
        exit(-1);
    }
    Time localTimePassedSinceCommit = time - clock->_lastCommitLocalTime;
    Time environmentTimePassedSinceCommit = localTimePassedSinceCommit;
    if (clock->_drift != 1.0) {
        double environmentTimePassedSinceCommitDoubleValue = (double)environmentTimePassedSinceCommit;
        environmentTimePassedSinceCommitDoubleValue = environmentTimePassedSinceCommitDoubleValue
                / clock->_drift;
        environmentTimePassedSinceCommit = (Time)environmentTimePassedSinceCommitDoubleValue;
    }
    Time environmentTime = clock->_lastCommitEnvironmentTime + environmentTimePassedSinceCommit;
    return environmentTime;
}
Time LocalClock_GetLocalTime(struct LocalClock* clock) {
    return clock->_localTime;
}
Time LocalClock_GetLocalTimeForCurrentEnvironmentTime(struct LocalClock* clock) {
    struct Director* container = clock->container;
    Time time = (*(container->getEnvironmentTime))(container);
    return (*(clock->getLocalTimeForEnvironmentTime))(clock, time);
}
Time LocalClock_GetLocalTimeForEnvironmentTime(struct LocalClock* clock, Time time) {
    if (clock->_lastCommitEnvironmentTime == -DBL_MAX
            || time < clock->_lastCommitEnvironmentTime) {
        fprintf(stderr, "Cannot compute environment time for local time %lf\
                                 because the last commit of the local time occured at \
                                 local time %lf : LocalClock_GetLocalTimeForEnvironmentTime\n",
                time, clock->_lastCommitLocalTime);
        exit(-1);
    }

    Time environmentTimePassedSinceCommit = time - clock->_lastCommitEnvironmentTime;
    Time localTimePassedSinceCommit = environmentTimePassedSinceCommit;
    if (clock->_drift != 1.0) {
        double localTimePassedSinceCommitDoubleValue = (double)environmentTimePassedSinceCommit;
        localTimePassedSinceCommitDoubleValue = localTimePassedSinceCommitDoubleValue
                                                * clock->_drift;
        localTimePassedSinceCommit = (Time)localTimePassedSinceCommitDoubleValue;
    }
    Time localTime = clock->_lastCommitEnvironmentTime - clock->_offset + localTimePassedSinceCommit;
    return localTime;
}
double LocalClock_GetTimeResolution(struct LocalClock* clock) {
    return clock->_timeResolution;
}
void LocalClock_Initialize(struct LocalClock* clock) {
    clock->_offset = 0.0;
}
void LocalClock_ResetLocalTime(struct LocalClock* clock, Time time) {
    clock->_localTime = time;
    _commit(clock);
}
void LocalClock_SetClockDrift(struct LocalClock* clock, double drift) {
    if (drift <= 0.0) {
        fprintf(stderr, "Clock drift has to be positive ! LocalClock_SetClockDrift\n");
        exit(-1);
    }
    clock->_drift = drift;
    _commit(clock);
}
void LocalClock_SetLocalTime(struct LocalClock* clock, Time time) {
    if (clock->_lastCommitLocalTime != -DBL_MAX
            && time < clock->_lastCommitLocalTime) {
        fprintf(stderr, "Cannot set local time to %lf\
                                  which is earlier than the last committed current time\
                                  %lf : LocalClock_GetLocalTimeForEnvironmentTime\n",
                time, clock->_lastCommitLocalTime);
        exit(-1);
    }
    clock->_localTime = time;
}
void LocalClock_SetTimeResolution(struct LocalClock* clock, double timeResolution) {
    clock->_timeResolution = timeResolution;
}
void LocalClock_Start(struct LocalClock* clock) {
    _commit(clock);
}
void LocalClock_Stop(struct LocalClock* clock) {
    _commit(clock);
}

void _commit(struct LocalClock* clock) {
    if (clock->_offset == -DBL_MAX) { // not initialized.
        return;
    }
    if (clock->_localTime != -DBL_MAX) {
        struct Director* container = clock->container;
        Time environmentTime = (*(container->getEnvironmentTime))(container);
        if (environmentTime == -DBL_MAX) {
            clock->_offset = 0.0;
        } else {
            clock->_offset = environmentTime - clock->_localTime;
        }
        clock->_lastCommitEnvironmentTime = environmentTime;
        clock->_lastCommitLocalTime = clock->_localTime;
    }
}
