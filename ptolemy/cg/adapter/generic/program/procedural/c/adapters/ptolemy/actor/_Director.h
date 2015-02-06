/* In this file we have defined a struct DIRECTOR which represent a generic director
 *
 * @author : William Lucas
 */

#ifndef DIRECTOR_H_
#define DIRECTOR_H_

#include "_IOPort.h"
#include "_Actor.h"
#include "_CompositeActor.h"
#include "_LocalClock.h"

#define DIRECTOR 0

struct Director {
        int typeDirector;

        struct CompositeActor* container;

        struct LocalClock* localClock;
        Time _startTime;
        Time _stopTime;

#ifdef _debugging
    char * _name;
    char *(*getName)(struct Director *);
    void (*setName)(struct Director *, char *);
#endif    
        void (*free)(struct Director*);

        void (*fire)(struct Director*);
        Time (*fireAt)(struct Director*, struct Actor*, Time, int);
        Time (*fireContainerAt)(struct Director*, Time, int);
        Time (*getEnvironmentTime)(struct Director*);
        Time (*getGlobalTime)(struct Director*);
        Time (*getModelStartTime)(struct Director*);
        Time (*getModelStopTime)(struct Director*);
        Time (*getModelTime)(struct Director*);
        void (*initialize)(struct Director*);
        void (*initialize1)(struct Director*, struct Actor*);
        bool (*isEmbedded)(struct Director*);
        int (*iterate)(struct Director*, int);
        bool (*postfire)(struct Director*);
        bool (*prefire)(struct Director*);
        void (*preinitialize)(struct Director*);
        void (*preinitialize1)(struct Director*, struct Actor*);
        bool (*transferInputs)(struct Director*, struct IOPort*);
        bool (*transferOutputs)(struct Director*);
        bool (*transferOutputs1)(struct Director*, struct IOPort*);
        void (*wrapup)(struct Director*);
        bool (*isTopLevel)(struct Director*);
};

struct Director* Director_New();
void Director_Init(struct Director* director);

#ifdef _debugging
char *Director_GetName(struct Director *director);
void Director_SetName(struct Director *director, char * name);
#endif

void Director_New_Free(struct Director* director);

void Director_Fire(struct Director* director);
Time Director_FireAt(struct Director* director, struct Actor*, Time time, int microstep);
Time Director_FireContainerAt(struct Director* director, Time time, int microstep);
Time Director_GetEnvironmentTime(struct Director* director);
Time Director_GetGlobalTime(struct Director* director);
Time Director_GetModelStartTime(struct Director* director);
Time Director_GetModelStopTime(struct Director* director);
Time Director_GetModelTime(struct Director* director);
void Director_Initialize(struct Director* director);
void Director_Initialize1(struct Director* director, struct Actor* actor);
bool Director_IsEmbedded(struct Director* director);
int Director_Iterate(struct Director* director, int count);
bool Director_Postfire(struct Director* director);
bool Director_Prefire(struct Director* director);
void Director_Preinitialize(struct Director* director);
void Director_Preinitialize1(struct Director* director, struct Actor* actor);
bool Director_TransferInputs(struct Director* director, struct IOPort* port);
bool Director_TransferOutputs(struct Director* director);
bool Director_TransferOutputs1(struct Director* director, struct IOPort* port);
void Director_Wrapup(struct Director* director);
bool Director_IsTopLevel(struct Director* director);

#endif /* DIRECTOR_H_ */
