/* In this file we have defined a struct SDFDirector which represent a SDF director
 *
 * @author : William Lucas
 */

#ifndef SDFDIRECTOR_H_
#define SDFDIRECTOR_H_

#include "_IOPort.h"
#include "_Actor.h"
#include "_CompositeActor.h"
#include "_LocalClock.h"

#define IS_SDFDIRECTOR(director) ((director)->typeDirector%10 == 2)

struct SDFDirector {
    int typeDirector;

    struct CompositeActor* container;

    struct LocalClock* localClock;
    Time _startTime;
    Time _stopTime;

    void (*free)(struct SDFDirector*);

    void (*fire)(struct SDFDirector*);
    Time (*fireAt)(struct Director*, struct Actor*, Time, int);
    Time (*fireContainerAt)(struct Director*, Time, int);
    Time (*getEnvironmentTime)(struct Director*);
    Time (*getGlobalTime)(struct Director*);
    Time (*getModelStartTime)(struct Director*);
    Time (*getModelStopTime)(struct Director*);
    Time (*getModelTime)(struct Director*);
    void (*initialize)(struct SDFDirector*);
    void (*initialize1)(struct Director*, struct Actor*);
    bool (*isEmbedded)(struct Director*);
    int (*iterate)(struct Director*, int);
    bool (*postfire)(struct SDFDirector*);
    bool (*prefire)(struct SDFDirector*);
    void (*preinitialize)(struct Director*);
    void (*preinitialize1)(struct Director*, struct Actor*);
    bool (*transferInputs)(struct SDFDirector*, struct IOPort*);
    bool (*transferOutputs)(struct Director*);
    bool (*transferOutputs1)(struct SDFDirector*, struct IOPort*);
    void (*wrapup)(struct Director*);
    bool (*isTopLevel)(struct Director*);

    // new members
    int (*getIterations)(struct SDFDirector*);
    Time (*getModelNextIterationTime)(struct SDFDirector*);
    void (*schedule)();

    int iterationCount;
    int iterations;
    Time period;
    bool _actorFinished;
    bool _prefire;
    bool _postfireReturns;
};

struct SDFDirector* SDFDirector_New();
void SDFDirector_Init(struct SDFDirector* director);
void SDFDirector_New_Free(struct SDFDirector* director);

int SDFDirector_GetIterations(struct SDFDirector* director);
Time SDFDirector_GetModelNextIterationTime(struct SDFDirector* director);
void SDFDirector_Fire(struct SDFDirector* director);
void SDFDirector_Initialize(struct SDFDirector* director);
bool SDFDirector_Postfire(struct SDFDirector* director);
bool SDFDirector_Prefire(struct SDFDirector* director);
bool SDFDirector_TransferInputs(struct SDFDirector* director, struct IOPort* port);
bool SDFDirector_TransferOutputs1(struct SDFDirector* director, struct IOPort* port);

#endif /* DEDIRECTOR_H_ */
