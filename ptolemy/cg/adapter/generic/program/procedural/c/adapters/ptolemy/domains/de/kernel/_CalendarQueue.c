#include "_CalendarQueue.h"

// Functions for a CalendarQueue :
struct CalendarQueue* CalendarQueue_New() {
	struct CalendarQueue* newQueue = malloc(sizeof(struct CalendarQueue));
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
	cqueue->_bottomThreshold;
	cqueue->_bucket;
	cqueue->_cachedMinimumBucket;
	cqueue->_indexOfMinimumBucket = 0;
	cqueue->_indexOfMinimumBucketValid = false;
	cqueue->_initialized = false;
	cqueue->_logMinNumBuckets = 1;
	cqueue->_logNumberOfBuckets;
	cqueue->_logQueueBinCountFactor = 1;
	cqueue->_minimumEntry = NULL;
	cqueue->_minVirtualBucket;
	cqueue->_minBucket;
	cqueue->_numberOfBuckets;
	cqueue->_numberOfBucketsMask;
	cqueue->_queueSize;
	cqueue->_resizeEnabled = true;
	cqueue->_topThreshold;

	cqueue->clear = CalendarQueue_Clear;
	cqueue->get = CalendarQueue_Get;
	cqueue->includes = CalendarQueue_Includes;
	cqueue->isEmpty = CalendarQueue_IsEmpty;
	cqueue->put = CalendarQueue_Put;
	cqueue->remove = CalendarQueue_Remove;
	cqueue->size = CalendarQueue_Size;
	cqueue->take = CalendarQueue_Take;
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
bool CalendarQueue_Includes(struct CalendarQueue* cqueue, struct DEEvent* entry) {
	if (cqueue->_queueSize == 0) {
		return false;
	}
	struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, (*(cqueue->_getBinIndex))(cqueue, entry));
	return (*(bucket->includes))(bucket, entry);
}
bool CalendarQueue_IsEmpty(struct CalendarQueue* cqueue) {
	return cqueue->_queueSize == 0;
}
bool CalendarQueue_Put(struct CalendarQueue* cqueue, struct DEEvent* entry) {
	if ((*(cqueue->includes))(cqueue, entry))
		return false;

	if (!cqueue->_initialized) {
		cqueue->_queueSize = 0;
		(*(cqueue->_localInit))(cqueue, cqueue->_logMinNumBuckets, entry);
	}

	int binNumber = (*(cqueue->_getBinIndex))(cqueue, entry);

	if (cqueue->_minimumEntry == NULL || cqueue->_queueSize == 0
			|| (*(entry->compareTo))(entry, cqueue->_minimumEntry) < 0) {
		cqueue->_minimumEntry = entry;
		cqueue->_minVirtualBucket = (*(entry->getVirtualBinNumber))(entry, cqueue->_binWidth);
		cqueue->_minBucket = (*(cqueue->_getBinIndex))(cqueue, entry);
	}

	struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, binNumber);

	if ((*(bucket->insert))(bucket, entry)) {
		++(cqueue->_queueSize);
		(*(cqueue->_resize))(cqueue, true);
	}

	return true;
}
bool CalendarQueue_Remove(struct CalendarQueue* cqueue, struct DEEvent* entry) {
	if (cqueue->_queueSize == 0) {
		return false;
	}

	struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, (*(cqueue->_getBinIndex))(cqueue, entry));
	boolean result = (*(bucket->remove))(bucket, entry);

	if (result) {
		cqueue->_queueSize--;
		(*(cqueue->_resize))(cqueue, false);
	}

	return result;
}
int CalendarQueue_Size(struct CalendarQueue* cqueue) {
	return cqueue->_queueSize;
}
void* CalendarQueue_Take(struct CalendarQueue* cqueue) {
	int indexOfMinimum = (*(cqueue->_getIndexOfMinimumBucket))(cqueue);
	return (*(cqueue->_takeFromBucket))(cqueue, indexOfMinimum);
}
int CalendarQueue__GetBinIndex(struct CalendarQueue* cqueue, struct DEEvent* entry) {
	long i = (*(entry->getVirtualBinNumber))(entry, cqueue->_binWidth);
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

			if ((*(minimumInBucket->getVirtualBinNumber))(minimumInBucket, cqueue->_binWidth) == cqueue->_minVirtualBucket	+ j) {
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
void CalendarQueue__LocalInit(struct CalendarQueue* cqueue, int logNumberOfBuckets, struct DEEvent* firstEntry) {
	cqueue->_logNumberOfBuckets = logNumberOfBuckets;
	cqueue->_numberOfBuckets = 1 << logNumberOfBuckets;
	cqueue->_numberOfBucketsMask = cqueue->_numberOfBuckets - 1;

	int numberOfBuckets = 1 << cqueue->_logNumberOfBuckets;
	cqueue->_bucket = pblListNewArrayList();

	for (int i = 0; i < numberOfBuckets; ++i) {
		pblListAdd(cqueue->_bucket, CQLinkedList_New());
	}

	cqueue->_minimumEntry = firstEntry;
	cqueue->_minVirtualBucket = (*(firstEntry->getVirtualBinNumber))(firstEntry, cqueue->_binWidth);
	cqueue->_minBucket = (*(cqueue->_getBinIndex))(cqueue, firstEntry);

	cqueue->_bottomThreshold = cqueue->_numberOfBuckets >> cqueue->_logQueueBinCountFactor;
	cqueue->_topThreshold = cqueue->_numberOfBuckets << cqueue->_logQueueBinCountFactor;
	cqueue->_queueSizeOverThreshold = cqueue->_queueSizeUnderThreshold = 0;
	cqueue->_initialized = true;
}
void CalendarQueue__Resize(struct CalendarQueue* cqueue, bool increasing) {
	cqueue->_indexOfMinimumBucketValid = false;

	if (!cqueue->_resizeEnabled) {
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
}
void* CalendarQueue__TakeFromBucket(struct CalendarQueue* cqueue, int index) {
	struct CQLinkedList* bucket = pblListGet(cqueue->_bucket, index);
	struct DEEvent* minEntry = (*(bucket->take))(bucket);

	cqueue->_minBucket = index;
	cqueue->_minimumEntry = minEntry;
	cqueue->_minVirtualBucket = (*(minEntry->getVirtualBinNumber))(minEntry, cqueue->_binWidth);
	--(cqueue->_queueSize);

	(*(cqueue->_resize))(cqueue, false);

	return minEntry;
}


// Functions for the CQLinkedList :

// Initialization of a list
struct CQLinkedList * CQLinkedList_New () {
	struct CQLinkedList * list = NULL;
	if ((list = malloc(sizeof(struct CQLinkedList))) == NULL) {
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
				&& item->_actor == p->content->_actor)
			return true;
	}
	return false;
}

// Tells if list is empty
bool CQLinkedList_IsEmpty(struct CQLinkedList * list) {
	return (list == NULL || list-> head == NULL);
}

// Adds item in list
bool CQLinkedList_Insert(struct CQLinkedList * list, struct DEEvent* item) {
	struct CQCell * newCell = NULL;

	if (list == NULL)
		return false;

	if (list->head == NULL) {
		list->head = CQCell_New();
		list->head->content = item;
		list->tail = list->head;
		return true;
	}

	if ((*(item->compareTo))(item, list->tail->content) >= 0) {
		newCell = CQCell_New();
		newCell->content = item;
		list->tail->next = newCell;
		list->tail = newCell;
		return true;
	}

	if ((*(item->compareTo))(item, list->head->content) < 0) {
		newCell = CQCell_New(item, list->head);
		newCell->content = item;
		newCell->next = list->head;
		list->head = newCell;
		return true;
	}

	struct CQCell * previousCell = list->head;
	struct CQCell * currentCell = previousCell->next;

	do {
		int comparison = (*(item->compareTo))(item, currentCell->content);
		if (comparison < 0) {
			newCell = CQCell_New();
			newCell->content = item;
			newCell->next = currentCell;
			previousCell->next = newCell;
			return true;
		}

		previousCell = currentCell;
		currentCell = previousCell->next;
	} while (currentCell != NULL);

	return false;
}

// Remove a selected item from the list
bool CQLinkedList_Remove(struct CQLinkedList * list, struct DEEvent* item) {
	if ((*(list->isEmpty))(list))
		return false;

	struct CQCell * head = list->head;
	struct CQCell * tail = list->tail;
	if (head == NULL) {
		return false;
	}

	if (head->content == item) {
		if (head != tail) {
			head = head->next;
		} else {
			head = NULL;
			tail = NULL;
		}
		return true;
	}

	struct CQCell * previousCell = head;
	struct CQCell * currentCell = previousCell->next;

	do {
		if (currentCell->content == item) {
			previousCell->next = currentCell->next;
			return true;
		}

		previousCell = currentCell;
		currentCell = currentCell->next;
	} while (currentCell != NULL);

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
	if (list == NULL)
		return;

	struct CQCell* cellTemp;
	struct CQCell* oldCellTemp;
	if (list->head == NULL)
		return;

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
	if ((cell = malloc(sizeof(struct CQCell))) == NULL) {
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
	if (cell)
		free(cell);
}
