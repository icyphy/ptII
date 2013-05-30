/* In this file we have defined 3 structures and functions related to those struct
 * CQCell, CQLinkedList and CalendarQueue
 * CQCell represents an element (DEEvent) and points to the next CQCell
 * CQLinkedList is basically a priority queue (contains a pointer to head and tail)
 * CQLinkedList is sorted by the DEEvent order
 * CalendarQueue encapsulate the queue (with also a size counter)
 *
 * @author : William Lucas
 */

#ifndef CALENDAR_QUEUE
#define CALENDAR_QUEUE

#include <stdlib.h>
#include <stdbool.h>
#include <errno.h>

#include "$ModelName()_DEEvent.h"

struct CQCell {
	DEEvent* content;
	struct CQCell* next;
};
typedef struct CQCell CQCell;
// functions for a CQCell
CQCell * newCQCell ();
CQCell * newCQCellWithParam (DEEvent* content, CQCell* next);
void CQCellDelete (CQCell * cell);

struct CQLinkedList {
	CQCell * head;
	CQCell * tail;
};
typedef struct CQLinkedList CQLinkedList;

// functions for a CQLinkedList
CQLinkedList * newCQLinkedList ();
CQLinkedList * newCQLinkedListWithParam (CQCell* head, CQCell* tail);
bool CQLinkedListIncludes(const CQLinkedList * list, const DEEvent * item);
bool CQLinkedListIsEmpty(const CQLinkedList * list);
bool CQLinkedListInsert(CQLinkedList * list, DEEvent* item);
bool CQLinkedListRemove(CQLinkedList * list, const DEEvent* item);
DEEvent * CQLinkedListTake(CQLinkedList * list);
DEEvent * CQLinkedListGet(CQLinkedList * list);
void CQLinkedListClear(CQLinkedList * list);
void CQLinkedListDelete(CQLinkedList * list);

/** First of all, we will only implement the CalendarQueue as a sorted LinkedList
 *  it should be fine for simple examples.
 *  If it works properly, we can upgrade this with a real CalendarQueue !
 */
struct CalendarQueue {
	struct CQLinkedList* bucket;
	int size;
};
typedef struct CalendarQueue CalendarQueue;

// functions for a CalendarQueue
CalendarQueue * newCQueue ();
void CQueueClear(CalendarQueue * cqueue);
void * CQueueGet(const CalendarQueue * cqueue);
bool CQueueIncludes(const CalendarQueue * cqueue, const void * item);
bool CQueueIsEmpty(const CalendarQueue * cqueue);
bool CQueuePut(struct CalendarQueue * cqueue, void * item);
bool CQueueRemove(CalendarQueue * cqueue, const void * item);
int CQueueSize(const CalendarQueue * cqueue);
void * CQueueTake(CalendarQueue * cqueue);
void CQueueDelete(CalendarQueue * cqueue);

#endif
