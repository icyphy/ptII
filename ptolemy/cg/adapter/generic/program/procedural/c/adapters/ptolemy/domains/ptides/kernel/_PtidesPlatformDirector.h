/* In this file we have defined the struct PtidesPlatformDirector
 *
 * @author : William Lucas
 */

#ifndef PTIDESPLATFORMDIRECTOR_H_
#define PTIDESPLATFORMDIRECTOR_H_

#include "_PtidesDirector.h"
#include "_PtidesMirrorPort.h"

#define IS_PTIDESPLATFORMDIRECTOR(d) ((d)->typeDirector%10 == 3)

struct PtidesPlatformDirector {
        int typeDirector;

        struct CompositeActor* container;

        struct LocalClock* localClock;
        Time _startTime;
        Time _stopTime;

#ifdef _debugging
    char * _name;
    char *(*getName)(struct PtidesPlatformDirector *);
    void (*setName)(struct PtidesPlatformDirector *, char *);
#endif
    
        void (*free)(struct PtidesPlatformDirector*);

        void (*fire)(struct PtidesPlatformDirector*);
        Time (*fireAt)(struct PtidesPlatformDirector*, struct Actor*, Time, int);
        Time (*fireContainerAt)(struct PtidesPlatformDirector*, Time, int);
        Time (*getEnvironmentTime)(struct PtidesPlatformDirector*);
        Time (*getGlobalTime)(struct PtidesPlatformDirector*);
        Time (*getModelStartTime)(struct PtidesPlatformDirector*);
        Time (*getModelStopTime)(struct PtidesPlatformDirector*);
        Time (*getModelTime)(struct PtidesPlatformDirector*);
        void (*initialize)(struct PtidesPlatformDirector*);
        void (*initialize1)(struct PtidesPlatformDirector*, struct Actor*);
        bool (*isEmbedded)(struct PtidesPlatformDirector*);
        int (*iterate)(struct PtidesPlatformDirector*, int);
        bool (*postfire)(struct PtidesPlatformDirector*);
        bool (*prefire)(struct PtidesPlatformDirector*);
        void (*preinitialize)(struct PtidesPlatformDirector*);
        void (*preinitialize1)(struct PtidesPlatformDirector*, struct Actor*);
        bool (*transferInputs)(struct PtidesPlatformDirector*, struct IOPort*);
        bool (*transferOutputs)(struct PtidesPlatformDirector*);
        bool (*transferOutputs1)(struct PtidesPlatformDirector*, struct PtidesPort*);
        void (*wrapup)(struct PtidesPlatformDirector*);
        bool (*isTopLevel)(struct PtidesPlatformDirector*);

        struct Director* (*_getEmbeddedPtidesDirector)(struct PtidesPlatformDirector*);
};

struct PtidesPlatformDirector* PtidesPlatformDirector_New();
void PtidesPlatformDirector_Init(struct PtidesPlatformDirector* director);
void PtidesPlatformDirector_New_Free(struct PtidesPlatformDirector* director);

void PtidesPlatformDirector_Fire(struct PtidesPlatformDirector* director);
Time PtidesPlatformDirector_FireContainerAt(struct PtidesPlatformDirector* director, Time time, int microstep);
Time PtidesPlatformDirector_GetEnvironmentTime(struct PtidesPlatformDirector* director);
bool PtidesPlatformDirector_Postfire(struct PtidesPlatformDirector* director);
bool PtidesPlatformDirector_Prefire(struct PtidesPlatformDirector* director) ;
bool PtidesPlatformDirector_TransferInputs(struct PtidesPlatformDirector* director, struct IOPort* port);
bool PtidesPlatformDirector_TransferOutputs1(struct PtidesPlatformDirector* director, struct PtidesPort* port);
struct Director* PtidesPlatformDirector__GetEmbeddedPtidesDirector(struct PtidesPlatformDirector* director);

#endif /* PTIDESPLATFORMDIRECTOR_H_ */
