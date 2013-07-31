/* In this file we have defined 3 structures and functions related to those struct
 * CQCell, CQLinkedList and CalendarQueue
 * CQCell represents an element (DEEvent) and points to the next CQCell
 * CQLinkedList is basically a priority queue (contains a pointer to head and tail)
 * CQLinkedList is sorted by the DEEvent order
 * CalendarQueue is a calendar queue
 *
 * @author : William Lucas
 */

#ifndef CALENDAR_QUEUE
#define CALENDAR_QUEUE

#include <stdlib.h>
#include <stdbool.h>
#include <errno.h>

#include "_DEEvent.h"

struct CQCell {
	struct DEEvent* content;
	struct CQCell* next;

	void (*free)(struct CQCell*);
};
// functions for a CQCell
struct CQCell * CQCell_New ();
void CQCell_Init(struct CQCell* cell);
void CQCell_New_Free (struct CQCell* cell);

struct CQLinkedList {
	struct CQCell * head;
	struct CQCell * tail;

	void (*free)(struct CQLinkedList*);

	bool (*includes)(struct CQLinkedList*, struct DEEvent*);
	bool (*isEmpty)(struct CQLinkedList*);
	bool (*insert)(struct CQLinkedList*, struct DEEvent*);
	bool (*remove)(struct CQLinkedList*, struct DEEvent*);
	struct DEEvent* (*take)(struct CQLinkedList*);
	struct DEEvent* (*get)(struct CQLinkedList*);
	void (*clear)(struct CQLinkedList*);
};

// functions for a CQLinkedList
struct CQLinkedList* CQLinkedList_New();
void CQLinkedList_Init(struct CQLinkedList* list);
bool CQLinkedList_Includes(struct CQLinkedList * list, struct DEEvent * item);
bool CQLinkedList_IsEmpty(struct CQLinkedList * list);
bool CQLinkedList_Insert(struct CQLinkedList * list, struct DEEvent* item);
bool CQLinkedList_Remove(struct CQLinkedList * list, struct DEEvent* item);
struct DEEvent * CQLinkedList_Take(struct CQLinkedList * list);
struct DEEvent * CQLinkedList_Get(struct CQLinkedList * list);
void CQLinkedList_Clear(struct CQLinkedList * list);
void CQLinkedList_New_Free(struct CQLinkedList * list);

#define _RESIZE_LAG 32
#define _SAMPLE_SIZE 8

struct CalendarQueue {
	void (*free)(struct CalendarQueue*);

	int _queueSizeOverThreshold;
	int _queueSizeUnderThreshold;
	double _binWidth;
	int _bottomThreshold;
	PblList* _bucket;
	void* _cachedMinimumBucket;
	int _indexOfMinimumBucket;
	bool _indexOfMinimumBucketValid;
	bool _initialized;
	int _logMinNumBuckets;
	int _logNumberOfBuckets;
	int _logQueueBinCountFactor;
	void* _minimumEntry;
	long _minVirtualBucket;
	int _minBucket;
	int _numberOfBuckets;
	int _numberOfBucketsMask;
	int _queueSize;
	bool _resizeEnabled;
	int _topThreshold;

	void (*clear)(struct CalendarQueue *);
	void* (*get)(struct CalendarQueue *);
	bool (*includes)(struct CalendarQueue *, struct DEEvent*);
	bool (*isEmpty)(struct CalendarQueue*);
	bool (*put)(struct CalendarQueue *, struct DEEvent*);
	bool (*remove)(struct CalendarQueue *, struct DEEvent*);
	int (*size)(struct CalendarQueue *);
	void* (*take)(struct CalendarQueue *);
	int (*_getBinIndex)(struct CalendarQueue *, struct DEEvent*);
	void* (*_getFromBucket)(struct CalendarQueue *, int);
	int (*_getIndexOfMinimumBucket)(struct CalendarQueue *);
	void (*_localInit)(struct CalendarQueue *, int, struct DEEvent*);
	void (*_resize)(struct CalendarQueue *, bool);
	void* (*_takeFromBucket)(struct CalendarQueue *, int);
};

struct CalendarQueue* CalendarQueue_New();
void CalendarQueue_Init(struct CalendarQueue* cqueue);
void CalendarQueue_New_Free(struct CalendarQueue* cqueue);

void CalendarQueue_Clear(struct CalendarQueue* cqueue);
void* CalendarQueue_Get(struct CalendarQueue* cqueue);
bool CalendarQueue_Includes(struct CalendarQueue* cqueue, struct DEEvent* entry);
bool CalendarQueue_IsEmpty(struct CalendarQueue* cqueue);
bool CalendarQueue_Put(struct CalendarQueue* cqueue, struct DEEvent* entry);
bool CalendarQueue_Remove(struct CalendarQueue* cqueue, struct DEEvent* entry);
int CalendarQueue_Size(struct CalendarQueue* cqueue);
void* CalendarQueue_Take(struct CalendarQueue* cqueue);
int CalendarQueue__GetBinIndex(struct CalendarQueue* cqueue, struct DEEvent* entry);
void* CalendarQueue__GetFromBucket(struct CalendarQueue* cqueue, int index);
int CalendarQueue__GetIndexOfMinimumBucket(struct CalendarQueue* cqueue);
void CalendarQueue__LocalInit(struct CalendarQueue* cqueue, int logNumberOfBuckets, struct DEEvent* firstEntry);
void CalendarQueue__Resize(struct CalendarQueue* cqueue, bool increasing);
void* CalendarQueue__TakeFromBucket(struct CalendarQueue* cqueue, int index);

#endif
