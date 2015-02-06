#include "_CalendarQueue.h"

// Define CQDEBUG to turn on debugging
//#define CQDEBUG 1

static bool _printing = false;

void _print(struct CalendarQueue* cqueue) {
#ifdef CQDEBUG
    _printing = true;
    printf("%p: ", (void *)cqueue);
    void** out = CalendarQueue_ToArray(cqueue);
    for (int outIndex = 0;  outIndex < cqueue->_queueSize; outIndex++) {
        printf("%p ", out[outIndex]);
    }
    printf("\n");
    free(out);
    _printing = false;
#endif
}

// Functions for a CalendarQueue :
struct CalendarQueue* CalendarQueue_New() {
    struct CalendarQueue* newQueue = calloc(1, sizeof(struct CalendarQueue));
    if (newQueue == NULL) {
        fprintf(stderr, "Allocation error : CalendarQueue_New\n");
        exit(-1);
    }
    CalendarQueue_Init(newQueue);
    newQueue->free = CalendarQueue_New_Free;

    return newQueue;
}
void CalendarQueue_Init(struct CalendarQueue* cqueue) {
    cqueue->_queueSizeOverThreshold = 0;
    cqueue->_queueSizeUnderThreshold = 0;
    cqueue->_binWidth = 2.0;
//        cqueue->_bottomThreshold;
//        cqueue->_bucket;
//        cqueue->_cachedMinimumBucket;
    cqueue->_indexOfMinimumBucket = 0;
    cqueue->_indexOfMinimumBucketValid = false;
    cqueue->_initialized = false;
    cqueue->_logMinNumBuckets = 1;
//        cqueue->_logNumberOfBuckets;
    cqueue->_logQueueBinCountFactor = 1;
    cqueue->_minimumEntry = NULL;
//        cqueue->_minVirtualBucket;
//        cqueue->_minBucket;
//        cqueue->_numberOfBuckets;
//        cqueue->_numberOfBucketsMask;
//        cqueue->_queueSize;
    cqueue->_resizeEnabled = true;
//        cqueue->_topThreshold;

    cqueue->clear = CalendarQueue_Clear;
    cqueue->get = CalendarQueue_Get;
    cqueue->includes = CalendarQueue_Includes;
    cqueue->isEmpty = CalendarQueue_IsEmpty;
    cqueue->put = CalendarQueue_Put;
    cqueue->remove = CalendarQueue_Remove;
    cqueue->size = CalendarQueue_Size;
    cqueue->take = CalendarQueue_Take;
    cqueue->toArray = CalendarQueue_ToArray;
    cqueue->_getBinIndex = CalendarQueue__GetBinIndex;
    cqueue->_getFromBucket = CalendarQueue__GetFromBucket;
    cqueue->_getIndexOfMinimumBucket = CalendarQueue__GetIndexOfMinimumBucket;
    cqueue->_localInit = CalendarQueue__LocalInit;
    cqueue->_resize = CalendarQueue__Resize;
    cqueue->_takeFromBucket = CalendarQueue__TakeFromBucket;
}
void CalendarQueue_New_Free(struct CalendarQueue* cqueue) {
    if (cqueue) {
        pblListFree(cqueue->_bucket);
        free(cqueue);
    }
}

void CalendarQueue_Clear(struct CalendarQueue* cqueue) {
    cqueue->_initialized = false;
    cqueue->_queueSize = 0;
    cqueue->_indexOfMinimumBucketValid = false;
    cqueue->_cachedMinimumBucket = NULL;
#ifdef CQDEBUG
    printf("CQ_Clear end %p %d\n", (void*) cqueue, cqueue->_queueSize);
#endif
}
void* CalendarQueue_Get(struct CalendarQueue* cqueue) {
    if (cqueue->_indexOfMinimumBucketValid) {
        return cqueue->_cachedMinimumBucket;
    } else {
        int indexOfMinimum = (*(cqueue->_getIndexOfMinimumBucket))(cqueue);
        void* result = (*(cqueue->_getFromBucket))(cqueue, indexOfMinimum);
        cqueue->_cachedMinimumBucket = result;
        return result;
    }
}
bool CalendarQueue_Includes(struct CalendarQueue* cqueue, void* entry) {
    if (cqueue->_queueSize == 0) {
        return false;
    }
    struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, (*(cqueue->_getBinIndex))(cqueue, entry));
    return (*(bucket->includes))(bucket, entry);
}
bool CalendarQueue_IsEmpty(struct CalendarQueue* cqueue) {
    return cqueue->_queueSize == 0;
}
bool CalendarQueue_Put(struct CalendarQueue* cqueue, void* entry) {
#ifdef CQDEBUG
    printf("CQ_Put start %p %d %p\n", (void*) cqueue, cqueue->_queueSize, (void *)entry);
    _print(cqueue);
#endif
    if ((*(cqueue->includes))(cqueue, entry)) {
        return false;
    }

    if (!cqueue->_initialized) {
        cqueue->_queueSize = 0;
        (*(cqueue->_localInit))(cqueue, cqueue->_logMinNumBuckets, entry);
    }
#ifdef CQDEBUG
    printf("CQ_Put after initialize check\n");
    _print(cqueue);
#endif
    int binNumber = (*(cqueue->_getBinIndex))(cqueue, entry);

    if (cqueue->_minimumEntry == NULL || cqueue->_queueSize == 0
            || (*(((struct DEEvent*)entry)->compareTo))(entry, cqueue->_minimumEntry) < 0) {
        cqueue->_minimumEntry = entry;
        cqueue->_minVirtualBucket = (*(((struct DEEvent*)entry)->getVirtualBinNumber))(entry, cqueue->_binWidth);
        cqueue->_minBucket = (*(cqueue->_getBinIndex))(cqueue, entry);
#ifdef CQDEBUG
        printf("CQ_Put set minimumEntry\n");
#endif
    }
#ifdef CQDEBUG
    printf("CQ_Put after minimumEntry check. binNumber: %d\n", binNumber);
    _print(cqueue);
#endif
    struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, binNumber);

    if ((*(bucket->insert))(bucket, entry)) {
        ++(cqueue->_queueSize);
        (*(cqueue->_resize))(cqueue, true);
    } else {
        printf("CQ_Put: false bucket insert?\n");
    }

#ifdef CQDEBUG
    printf("CQ_Put end %p %d\n", (void*) cqueue, cqueue->_queueSize);
    _print(cqueue);
#endif
    return true;
}

bool CalendarQueue_Remove(struct CalendarQueue* cqueue, void* entry) {
#ifdef CQDEBUG
    printf("CQ_Remove start %p %d %p\n", (void*) cqueue, cqueue->_queueSize, (void *) entry);
#endif
    if (cqueue->_queueSize == 0) {
        return false;
    }

    struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, (*(cqueue->_getBinIndex))(cqueue, entry));
    boolean result = (*(bucket->remove))(bucket, entry);

    if (result) {
        cqueue->_queueSize--;
        (*(cqueue->_resize))(cqueue, false);
    }

#ifdef PTIDESDIRECTOR
    if (IS_PTIDESEVENT((struct DEEvent*)entry) && ((struct PtidesEvent*)entry)->receiver(entry) != NULL) {
        struct PtidesEvent* ptidesEvent = (struct PtidesEvent*) entry;
        if (IS_PTIDESRECEIVER(ptidesEvent->receiver(ptidesEvent))) {
            struct PtidesReceiver* ptidesReceiver = (struct PtidesReceiver*) ptidesEvent->receiver(ptidesEvent);
            ptidesReceiver->putToReceiver(ptidesReceiver, ptidesEvent->token(ptidesEvent));
        }
    }
#endif

#ifdef CQDEBUG
    printf("CQ_Remove end %p %d\n", (void*) cqueue, cqueue->_queueSize);
    _print(cqueue);
#endif
    return result;
}
int CalendarQueue_Size(struct CalendarQueue* cqueue) {
    //printf("CQ_Size %p %d\n", (void*) cqueue, cqueue->_queueSize);
    return cqueue->_queueSize;
}
void* CalendarQueue_Take(struct CalendarQueue* cqueue) {
    int indexOfMinimum = (*(cqueue->_getIndexOfMinimumBucket))(cqueue);
    void* event = (*(cqueue->_takeFromBucket))(cqueue, indexOfMinimum);
    return event;
}

// Used for error messages
static int _emptyQueueErrorMessageCount = 0;

void** CalendarQueue_ToArray(struct CalendarQueue* cqueue) {
    int size = cqueue->size(cqueue);
    void** result = calloc(size, sizeof(void*));
    if (!result) {
        fprintf(stderr, "Allocation error : CalendarQueue_ToArray");
        exit(-1);
    }
    if (size == 0) {
        return result;
    }
    int index = 0;
    int currentBucket = cqueue->_minBucket;
    long virtualBucket = cqueue->_minVirtualBucket;
    long minimumNextVirtualBucket = INT_MAX;
    bool foundValue = false;
    int nextStartBucket = cqueue->_minBucket;

    struct CQCell** bucketHead = calloc(pblListSize(cqueue->_bucket), sizeof(struct CQCell*));

    for (int i = 0; i < pblListSize(cqueue->_bucket); i++) {
        struct CQLinkedList* bucket = (struct CQLinkedList*)pblListGet(cqueue->_bucket, i);
        bucketHead[i] = bucket->head;
    }

    while (true) {
        if (bucketHead[currentBucket] != NULL) {
            void* nextInBucket = bucketHead[currentBucket]->content;

            while ((*(((struct DEEvent*)nextInBucket)->getVirtualBinNumber))(nextInBucket, cqueue->_binWidth) == virtualBucket) {
                result[index] = nextInBucket;
                index++;
                if (index == size) {
                    free(bucketHead);
                    return result;
                }
                bucketHead[currentBucket] = bucketHead[currentBucket]->next;
                if (bucketHead[currentBucket] == NULL) {
                    break;
                }
                nextInBucket = bucketHead[currentBucket]->content;
            }
            long nextVirtualBucket = (*(((struct DEEvent*)nextInBucket)->getVirtualBinNumber))(nextInBucket, cqueue->_binWidth);
            if (nextVirtualBucket < minimumNextVirtualBucket
                    || nextVirtualBucket == INT_MAX) {
                foundValue = true;
                minimumNextVirtualBucket = nextVirtualBucket;
                nextStartBucket = currentBucket;
            }
        }
        ++currentBucket;
        ++virtualBucket;
        if (currentBucket == cqueue->_numberOfBuckets) {
            currentBucket = 0;
        }
        if (currentBucket == nextStartBucket) {
            if (!foundValue) {
                fprintf(stderr,
                        "CalendarQueue %p: Queue is empty, but size() is not zero! It is: %i.  CurrentBucket: %d, _numberOfBuckets: %d\n",
                        (void *) cqueue,
                        cqueue->_queueSize, currentBucket, cqueue->_numberOfBuckets);
                int maxMessage = 5;
                if (_emptyQueueErrorMessageCount++ > maxMessage) {
                    fprintf(stderr, "Printed %d Queue empty messages. Queue was:\n", maxMessage);
                    if (!_printing) {
                        _print(cqueue);
                    }
                    printf("\nExiting.\n");
                    exit(-1);
                }
            }
            virtualBucket = minimumNextVirtualBucket;
            foundValue = false;
            minimumNextVirtualBucket = INT_MAX;
        }
    }
    free(bucketHead);
    return result;
}

int CalendarQueue__GetBinIndex(struct CalendarQueue* cqueue, void* entry) {
    long i = (*(((struct DEEvent*)entry)->getVirtualBinNumber))(entry, cqueue->_binWidth);
    i = i & cqueue->_numberOfBucketsMask;
    return (int) i;
}
void* CalendarQueue__GetFromBucket(struct CalendarQueue* cqueue, int index) {
    struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, index);
    return bucket->head->content;
}
int CalendarQueue__GetIndexOfMinimumBucket(struct CalendarQueue* cqueue) {
    if (cqueue->_queueSize == 0) {
        fprintf(stderr, "Queue is empty. CalendarQueue__GetIndexOfMinimumBucket");
        exit(-1);
    }

    if (cqueue->_indexOfMinimumBucketValid) {
        return cqueue->_indexOfMinimumBucket;
    }

    int i = cqueue->_minBucket;
    int j = 0;
    int indexOfMinimum = i;
    struct DEEvent* minSoFar = NULL;

    while (true) {
        struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, i);
        if (!(*(bucket->isEmpty))(bucket)) {
            struct DEEvent* minimumInBucket = bucket->head->content;

            if ((*(minimumInBucket->getVirtualBinNumber))(minimumInBucket, cqueue->_binWidth) == cqueue->_minVirtualBucket        + j) {
                cqueue->_indexOfMinimumBucket = i;
                break;
            } else {
                if (minSoFar == NULL) {
                    minSoFar = minimumInBucket;
                    indexOfMinimum = i;
                } else if ((*(minimumInBucket->compareTo))(minimumInBucket, minSoFar) < 0) {
                    minSoFar = minimumInBucket;
                    indexOfMinimum = i;
                }
            }
        }

        // Prepare to check the next bucket
        ++i;
        ++j;

        if (i == cqueue->_numberOfBuckets) {
            i = 0;
        }

        if (i == cqueue->_minBucket) {
            if (minSoFar == NULL) {
                fprintf(stderr, "Queue is empty, but size() is not zero!");
                exit(-1);
            }
            cqueue->_indexOfMinimumBucket = indexOfMinimum;
            break;
        }
    }

    cqueue->_indexOfMinimumBucketValid = true;
    return cqueue->_indexOfMinimumBucket;
}
void CalendarQueue__LocalInit(struct CalendarQueue* cqueue, int logNumberOfBuckets, void* firstEntry) {
    cqueue->_logNumberOfBuckets = logNumberOfBuckets;
    cqueue->_numberOfBuckets = 1 << logNumberOfBuckets;
    cqueue->_numberOfBucketsMask = cqueue->_numberOfBuckets - 1;

    int numberOfBuckets = 1 << cqueue->_logNumberOfBuckets;
    cqueue->_bucket = pblListNewArrayList();

    for (int i = 0; i < numberOfBuckets; ++i) {
        pblListAdd(cqueue->_bucket, CQLinkedList_New());
    }

    cqueue->_minimumEntry = firstEntry;
    cqueue->_minVirtualBucket = (*(((struct DEEvent*)firstEntry)->getVirtualBinNumber))(firstEntry, cqueue->_binWidth);
    cqueue->_minBucket = (*(cqueue->_getBinIndex))(cqueue, firstEntry);

    cqueue->_bottomThreshold = cqueue->_numberOfBuckets >> cqueue->_logQueueBinCountFactor;
    cqueue->_topThreshold = cqueue->_numberOfBuckets << cqueue->_logQueueBinCountFactor;
    cqueue->_queueSizeOverThreshold = cqueue->_queueSizeUnderThreshold = 0;
    cqueue->_initialized = true;
}
void CalendarQueue__Resize(struct CalendarQueue* cqueue, bool increasing) {
#ifdef CQDEBUG
    printf("CQ_Resize start %p %d\n", (void*) cqueue, cqueue->_queueSize);
    //_print(cqueue);
#endif
    cqueue->_indexOfMinimumBucketValid = false;

    if (!cqueue->_resizeEnabled) {
#ifdef CQDEBUG
        printf("CQ_Resize end: resize not enabled %p %d\n", (void*) cqueue, cqueue->_queueSize);
#endif
        return;
    }

    int logNewSize = cqueue->_logNumberOfBuckets;
    bool resize = false;

    if (increasing) {
        if (cqueue->_queueSize > cqueue->_topThreshold) {
            cqueue->_queueSizeOverThreshold++;
        }

        if (cqueue->_queueSizeOverThreshold > _RESIZE_LAG) {
            resize = true;
            cqueue->_queueSizeOverThreshold = 0;
            logNewSize = cqueue->_logNumberOfBuckets + cqueue->_logQueueBinCountFactor;
        }
    } else {
        if (cqueue->_queueSize < cqueue->_bottomThreshold) {
            cqueue->_queueSizeUnderThreshold++;
        }

        if (cqueue->_queueSizeUnderThreshold > _RESIZE_LAG) {
            resize = true;
            cqueue->_queueSizeUnderThreshold = 0;

            int tempLogNewSize = cqueue->_logNumberOfBuckets
                                 - cqueue->_logQueueBinCountFactor;

            if (tempLogNewSize > cqueue->_logMinNumBuckets) {
                logNewSize = tempLogNewSize;
            }
        }
    }

    if (!resize) {
#ifdef CQDEBUG
        printf("CQ_Resize end: not resizing %p %d\n", (void*) cqueue, cqueue->_queueSize);
        _print(cqueue);
#endif
        return;
    }

    PblList* old_bucket = cqueue->_bucket;
    int old_numberOfBuckets = cqueue->_numberOfBuckets;

    (*(cqueue->_localInit))(cqueue, logNewSize, cqueue->_minimumEntry);
    cqueue->_queueSize = 0;

    bool saveResizeEnabled = cqueue->_resizeEnabled;
    cqueue->_resizeEnabled = false;

    for (int i = 0; i < old_numberOfBuckets; i++) {
        struct CQLinkedList* bucket = pblListGet(old_bucket, i);
        while (!(*(bucket->isEmpty))(bucket)) {
            (*(cqueue->put))(cqueue, (*(bucket->take))(bucket));
        }
    }
    pblListFree(old_bucket);

    cqueue->_resizeEnabled = saveResizeEnabled;
#ifdef CQDEBUG
    printf("CQ_Resize end %p %d\n", (void*) cqueue, cqueue->_queueSize);
#endif
}
void* CalendarQueue__TakeFromBucket(struct CalendarQueue* cqueue, int index) {
    struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, index);
    struct DEEvent* minEntry = (*(bucket->take))(bucket);

#ifdef CQDEBUG
    printf("CQ_TakeFromBucket start %p %d\n", (void*) cqueue, cqueue->_queueSize);
#endif
    cqueue->_minBucket = index;
    cqueue->_minimumEntry = minEntry;
    cqueue->_minVirtualBucket = (*(minEntry->getVirtualBinNumber))(minEntry, cqueue->_binWidth);
    --(cqueue->_queueSize);


    (*(cqueue->_resize))(cqueue, false);

#ifdef CQDEBUG
    printf("CQ_TakeFromBucket end: returning minEntry %p %d\n", (void*) cqueue, cqueue->_queueSize);
#endif

    return minEntry;
}


// Functions for the CQLinkedList
// This is a multiset, an entry can appear more than once.

// Initialization of a list
struct CQLinkedList * CQLinkedList_New () {
    struct CQLinkedList * list = NULL;
    if ((list = calloc(1, sizeof(struct CQLinkedList))) == NULL) {
        fprintf(stderr, "Allocation Error (CQLinkedList_New)");
        exit(-1);
    }
    list->free = CQLinkedList_New_Free;
    CQLinkedList_Init(list);
    return list;
}
void CQLinkedList_Init(struct CQLinkedList* list) {
    list->head = NULL;
    list->tail = NULL;

    list->includes = CQLinkedList_Includes;
    list->isEmpty = CQLinkedList_IsEmpty;
    list->insert = CQLinkedList_Insert;
    list->remove = CQLinkedList_Remove;
    list->take = CQLinkedList_Take;
    list->get = CQLinkedList_Get;
    list->clear = CQLinkedList_Clear;
}
// Search if the DEEvent item is in list
bool CQLinkedList_Includes(struct CQLinkedList * list, struct DEEvent * item) {
    if ((*(list->isEmpty))(list))
        return false;
    struct CQCell * p = NULL;
    for (p = list->head ; p != NULL; p = p->next)  {
        if ((*(item->compareTo))(item, p->content) == 0
                && item->_actor == p->content->_actor) {
#ifdef PTIDESDIRECTOR
            // In the case of ptides events we have to compare the receivers !
            if (IS_PTIDESEVENT(item) && IS_PTIDESEVENT(p->content))
                return ((struct PtidesEvent*) item)->_receiver == ((struct PtidesEvent*) p->content)->_receiver;
            else
#endif
                return true;
        }
    }
    return false;
}

// Tells if list is empty
bool CQLinkedList_IsEmpty(struct CQLinkedList * list) {
    return (list == NULL || list-> head == NULL);
}

void CQLinkedList_Print(struct CQLinkedList * list) {
    printf("CQLinkedList: %p: head: %p, tail: %p:", (void*)list, (void*)list->head, (void*)list->tail);
    struct CQCell * p = NULL;
    boolean sawTail = false;
    for (p = list->head ; p != NULL; p = p->next)  {
        if (p == list->tail) {
            printf("TAIL:");
            sawTail = true;
        }
        printf("(%p: %p), ", (void*)p, (void*)p->content);
    }
    if (list->head != NULL && !sawTail) {
        printf("!!Did not see tail???");
    }
    printf("\n");
}

// Functions for CQCell :
// Adds item in list
bool CQLinkedList_Insert(struct CQLinkedList * list, struct DEEvent* item) {
    struct CQCell * newCell = NULL;
#ifdef CQDEBUG
    printf("CQ_LinkedList_Insert start %p %p\n", (void*) list, (void*) item);
    CQLinkedList_Print(list);
#endif

    if (list == NULL) {
#ifdef CQDEBUG
        printf("CQ_LinkedList_Insert end: list was null %p %p\n", (void*) list, (void*) item);
#endif
        return false;
    }

    // Special case: linked list is empty.
    if (list->head == NULL) {
        list->head = CQCell_New();
        list->head->content = item;
        list->tail = list->head;
#ifdef CQDEBUG
        printf("CQ_LinkedList_Insert end: list head was null %p %p\n", (void*) list, (void*) item);
#endif
        return true;
    }

    // LinkedList is not empty.
    // I assert that by construction, when head != null,
    // then tail != null as well.
    // Special case: Check if object is greater than or equal to tail.
    if ((*(item->compareTo))(item, list->tail->content) >= 0) {
#ifdef CQDEBUG
        int result = (*(item->compareTo))(item, list->tail->content);
#endif
        // object becomes new tail.
        struct CQCell *newTail = CQCell_New();
        newTail->content = item;
        list->tail->next = newTail;
        list->tail = newTail;
#ifdef CQDEBUG
        printf("CQ_LinkedList_Insert end: tail %d >=0. return true. %p %p\n", result, (void*) list, (void*) item);
        CQLinkedList_Print(list);
#endif

        return true;
    }

    // Check if head is strictly greater than object.
    if ((*(item->compareTo))(list->head->content, item) > 0) {
        // object becomes the new head
        newCell = CQCell_New();
        newCell->content = item;
        newCell->next = list->head;
        list->head = newCell;
#ifdef CQDEBUG
        int result = (*(item->compareTo))(item, list->tail->content);
        printf("CQ_LinkedList_Insert end: head %d <0. return true. %p %p\n", result, (void*) list, (void*) item);
#endif
        return true;
    }

    struct CQCell * previousCell = list->head;
    struct CQCell * currentCell = previousCell->next;

    // Note that this loop will always terminate via the return
    // statement. This is because tail is assured of being strictly
    // greater than object.
    do {
        // check if currentCell is strictly greater than object
        int comparison = (*(item->compareTo))(currentCell->content, item);
        if (comparison > 0) {
            newCell = CQCell_New();
            newCell->content = item;
            newCell->next = currentCell;
            previousCell->next = newCell;
#ifdef CQDEBUG
            printf("CQ_LinkedList_Insert end: do loop, return true.  %p %p\n", (void*) list, (void*) item);
#endif

            return true;
        }

        previousCell = currentCell;
        currentCell = previousCell->next;
    } while (currentCell != NULL);

#ifdef CQDEBUG
    printf("CQ_LinkedList_Insert end: return false. %p %p\n", (void*) list, (void*) item);
#endif
    return false;
}

// Remove the specified element from the queue, where equals() is used
// to determine a match.  Only the first matching element that is found
// is removed. Return true if a matching element is found and removed,
// and false otherwise.
bool CQLinkedList_Remove(struct CQLinkedList * list, struct DEEvent* item) {
#ifdef CQDEBUG
    printf("CQ_LinkedList_Remove start %p %p\n", (void*) list, (void*) item);
    CQLinkedList_Print(list);
#endif
    if ((*(list->isEmpty))(list)) {
        return false;
    }

    struct CQCell * head = list->head;
    struct CQCell * tail = list->tail;
    // two special cases:
    // Case 1: list is empty: always return false.
    if (head == NULL) {
#ifdef CQDEBUG
        printf("CQ_LinkedList_Remove head was null %p %p\n", (void*) list, (void*) item);
        CQLinkedList_Print(list);
#endif
        return false;
    }

    // Case 2: The element I want is at head of the list.
    if (head->content == item) {
        if (head != tail) {
            // Linked list has at least two cells.
            list->head = head->next;
        } else {
            // Linked list contains only one cell
            list->head = NULL;
            list->tail = NULL;
        }
#ifdef CQDEBUG
        printf("CQ_LinkedList_Remove head was item %p %p\n", (void*) list, (void*) item);
        CQLinkedList_Print(list);
#endif
        return true;
    }

    // Non-special case that requires looping:
    struct CQCell * previousCell = head;
    struct CQCell * currentCell = previousCell->next;


    // Case where there is only one item in the queue
    if (currentCell == NULL) {
#ifdef CQDEBUG
        printf("CQ_LinkedList_Remove only one item in the queue %p %p\n", (void*) list, (void*) item);
        CQLinkedList_Print(list);
#endif
        return false;
    }

    do {
        if (currentCell->content == item) {
            // Found a match.
            if (list->tail == currentCell) {
                // Removing the tail. Need to update.
                list->tail = previousCell;
            }
            previousCell->next = currentCell->next;
#ifdef CQDEBUG
            printf("CQ_LinkedList_Remove inside do %p %p\n", (void*) list, (void*) item);
            CQLinkedList_Print(list);
#endif
            return true;
        }

        previousCell = currentCell;
        currentCell = currentCell->next;
    } while (currentCell != NULL);

#ifdef CQDEBUG
    printf("CQ_LinkedList_Remove end  %p %p\n", (void*) list, (void*) item);
    CQLinkedList_Print(list);
#endif
    return false;
}

// Return the first element of the list and depop it !
struct DEEvent* CQLinkedList_Take(struct CQLinkedList * list) {
    if ((*(list->isEmpty))(list))
        return NULL;
    // remove the head
    struct CQCell * oldHead = list->head;
    list->head = list->head->next;

    if (list->head == NULL) {
        list->tail = NULL;
    }
    return oldHead->content;
}

// Return the first element of the list
struct DEEvent * CQLinkedList_Get(struct CQLinkedList * list) {
    if ((*(list->isEmpty))(list))
        return NULL;
    return list->head->content;
}

// Clears completely (and properly) the list
void CQLinkedList_Clear(struct CQLinkedList * list) {
    if (list == NULL) {
        return;
    }

    struct CQCell* cellTemp;
    struct CQCell* oldCellTemp;
    if (list->head == NULL) {
        return;

    }
    if (list->head == list->tail) {
        (*(list->head->free))(list->head);
        return;
    }

    for (oldCellTemp = list->head, cellTemp = list->head->next ;
            cellTemp == NULL ;
            oldCellTemp = cellTemp, cellTemp = cellTemp->next) {
        (*(oldCellTemp->free))(oldCellTemp);
    }
    return;
}

// Delete the list forever (a long time)
void CQLinkedList_New_Free(struct CQLinkedList * list) {
    if (list) {
        (*(list->clear))(list);
        free(list);
    }
}

// Functions for CQCell :
struct CQCell * CQCell_New () {
    struct CQCell* cell = NULL;
    if ((cell = calloc(1, sizeof(struct CQCell))) == NULL) {
        fprintf(stderr, "Allocation Error (CQCell_New)");
        exit(-1);
    }
    cell->free = CQCell_New_Free;

    CQCell_Init(cell);
    return cell;
}
void CQCell_Init(struct CQCell* cell) {
    cell->content = NULL;
    cell->next = NULL;
}

// Free cell (not a joke) !
void CQCell_New_Free (struct CQCell * cell)
{
    if (cell) {
        free(cell);
    }
}
