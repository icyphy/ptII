/* C implementation of CompositeActor.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_CompositeActor.c
 */

#include "_CompositeActor.h"

void CompositeActor_Fire(struct CompositeActor* actor);
void CompositeActor_Initialize(struct CompositeActor* actor);
bool CompositeActor_Postfire(struct CompositeActor* actor);
bool CompositeActor_Prefire(struct CompositeActor* actor);
void CompositeActor_Preinitialize(struct CompositeActor* actor);
void CompositeActor_Wrapup(struct CompositeActor* actor);

struct CompositeActor* CompositeActor_New() {
    struct CompositeActor* newActor = calloc(1, sizeof(struct CompositeActor));
    if (newActor == NULL) {
        fprintf(stderr, "Allocation error : CompositeActor_New\n");
        exit(-1);
    }
    CompositeActor_Init(newActor);
    newActor->free = CompositeActor_New_Free;

    return newActor;
}
void CompositeActor_Init(struct CompositeActor* actor) {
    Actor_Init((struct Actor*)actor);

    actor->_inputPorts = pblListNewArrayList();
    actor->_outputPorts = pblListNewArrayList();
    actor->_containedEntities = pblListNewArrayList();
    actor->_director = NULL;

    actor->getDirector = CompositeActor_GetDirector;
    actor->getExecutiveDirector = CompositeActor_GetExecutiveDirector;
    actor->inputPortList = CompositeActor_InputPortList;
    actor->outputPortList = CompositeActor_OutputPortList;
    actor->isOpaque = CompositeActor_IsOpaque;
    actor->_transferPortParameterInputs = CompositeActor__TransferPortParameterInputs;

    actor->fire = CompositeActor_Fire;
    actor->initialize = CompositeActor_Initialize;
    actor->iterate = CompositeActor_Iterate;
    actor->postfire = CompositeActor_Postfire;
    actor->prefire = CompositeActor_Prefire;
    actor->preinitialize = CompositeActor_Preinitialize;
    actor->wrapup = CompositeActor_Wrapup;

#ifdef _debugging
    actor->_name = NULL;
    actor->getFullName = CompositeActor_GetFullName;
    actor->getName = CompositeActor_GetName;
    actor->setName = CompositeActor_SetName;
#endif

}
void CompositeActor_New_Free(struct CompositeActor* actor) {
    if (actor) {
        pblListFree(actor->_inputPorts);
        pblListFree(actor->_outputPorts);
    }
    Actor_New_Free((struct Actor*)actor);
}

struct Director* CompositeActor_GetDirector(struct CompositeActor* actor) {
    if (actor->_director != NULL) {
        return actor->_director;
    }
    struct CompositeActor* container = actor->container;
    return (*(container->getDirector))(container);
}
struct Director* CompositeActor_GetExecutiveDirector(struct CompositeActor* actor) {
    if (actor == NULL) {
        return NULL;
    }
    struct CompositeActor* container = actor->container;
    if (actor->container == NULL) {
        return NULL;
    } else {
        return (*(container->getDirector))(container);
    }
}
PblList* CompositeActor_InputPortList(struct CompositeActor* actor) {
    return actor->_inputPorts;
}
PblList* CompositeActor_OutputPortList(struct CompositeActor* actor) {
    return actor->_outputPorts;
}
bool CompositeActor_IsOpaque(struct CompositeActor* actor) {
    return actor->_director != NULL;
}
void CompositeActor__TransferPortParameterInputs(struct CompositeActor* actor) {
//        PblIterator* inputPorts = pblIteratorNew((*(actor->inputPortList))(actor));
//        while (pblIteratorHasNext(inputPorts)) {
//                struct IOPort* p = pblIteratorNext(inputPorts);
//
//                if (IS_PARAMETERPORT(p)) {
//                        struct PortParameter* parameter = (*(p->getParameter))(p);
//                        (*(parameter->update))(parameter);
//                }
//        }
}
static int _didNotTransferCount = 0;
void CompositeActor_Fire(struct CompositeActor* actor) {
    (*(actor->_transferPortParameterInputs))(actor);

    PblIterator* inputPorts = pblIteratorNew((*(actor->inputPortList))(actor));
    while (pblIteratorHasNext(inputPorts)) {
        struct IOPort* p = (struct IOPort*) pblIteratorNext(inputPorts);

        // FIXME : if (!(p instanceof ParameterPort)) {
        if (!(*(actor->_director->transferInputs))(actor->_director, p)) {
            if (_didNotTransferCount++ < 10) {
                fprintf(stderr, "%s:%d: CompositeActorFire():(%d) director did not transfer inputs?.\n", __FILE__, __LINE__, _didNotTransferCount);
            } else {
                if (_didNotTransferCount++ == 10) {
                    fprintf(stderr, "%s:%d: CompositeActorFire(): printed \"director did not transfer inputs?\" 10 times, no longer printing.\n", __FILE__, __LINE__);
                }
            }
            // Don't return here or else this will fail:                                                               // $PTII/bin/ptcg -generatorPackage ptolemy.cg.kernel.generic.program.procedural.c $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/lib/test/auto/Network.xml
            //return;
            //exit(-1);
        }
        //}
    }
    //MEMORY_FIX: Uncommented link to free memory
    pblIteratorFree(inputPorts);

    (*(actor->_director->fire))(actor->_director);

    (*(actor->_director->transferOutputs))(actor->_director);
}
void CompositeActor_Initialize(struct CompositeActor* actor) {
    struct Director* director = (*(actor->getDirector))(actor);
    (*(director->initialize))(director);
}
int CompositeActor_Iterate(struct CompositeActor* actor, int count) {
    int n = 0;

    while (n++ < count) {
        if ((*(actor->prefire))(actor)) {
            (*(actor->fire))(actor);
            if (!(*(actor->postfire))(actor))
                return n;
        } else {
            return -1;
        }
    }
    return count;
}
bool CompositeActor_Postfire(struct CompositeActor* actor) {
    struct Director* director = (*(actor->getDirector))(actor);
    bool result = (*(director->postfire))(director);
    return result;
}
bool CompositeActor_Prefire(struct CompositeActor* actor) {
    struct Director* director = (*(actor->getDirector))(actor);
    bool result = (*(director->prefire))(director);
    return result;
}
void CompositeActor_Preinitialize(struct CompositeActor* actor) {
    struct Director* director = (*(actor->getDirector))(actor);
    (*(director->preinitialize))(director);
}
void CompositeActor_Wrapup(struct CompositeActor* actor) {
    struct Director* director = (*(actor->getDirector))(actor);
    (*(director->wrapup))(director);
}

#ifdef _debugging
// To enable debugging, recompile the code with:
//    make -f *.mk "USER_CC_FLAGS=-D _debugging" run

/* Return the full name of the composite.
 * The caller should free the results returned by this method.
 */
char *CompositeActor_GetFullName(struct CompositeActor *compositeActor) {
    char *containerFullName;
    if (compositeActor->container != NULL) {
        containerFullName = compositeActor->container->getFullName(compositeActor->container);
    } else {
        containerFullName = strdup(".");
    }

    char *results = NULL;
    if (compositeActor->_name != NULL) {
        results = malloc(strlen(containerFullName) + 1 /* For the dot */ + strlen(compositeActor->_name) + 1);
    } else {
        if (compositeActor->container != NULL) {
            results = malloc(strlen(containerFullName) + 1 + strlen("Unnamed") + 1);
        } else {
            results = malloc(2);
        }
    }

    strcpy(results, containerFullName);
    free(containerFullName);
    if (compositeActor->container != NULL) {
        strcat(results, ".");
    }
    if (compositeActor->_name != NULL) {
        strcat(results, compositeActor->_name);
    } else {
        strcat(results, "Unnamed");
    }
    return results;
}

/* Return the name of this composite.
 * The caller should free the results returned by this method.
 */
char *CompositeActor_GetName(struct CompositeActor *compositeActor) {
    return strdup(compositeActor->_name);
}
void CompositeActor_SetName(struct CompositeActor *compositeActor, char * name) {
    compositeActor->_name = strdup(name);
}
#endif
