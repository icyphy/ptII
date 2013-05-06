#include "../includes/CalendarQueue.h"

// Functions for a CalendarQueue :

// Initialization of a CQueue (temporary)
CalendarQueue * newCQueue () {
	CalendarQueue * cqueue = NULL;
	if (cqueue == NULL) {
		cqueue = malloc(sizeof(CalendarQueue));
		if (cqueue == NULL)
			perror("Allocation problem (CQueueInitialize)");
	}

	if (cqueue->bucket != NULL)
		CQLinkedListDelete(cqueue->bucket);

	cqueue->bucket = newCQLinkedList();
	cqueue->size = 0;
	return cqueue;
}

// Delete all the elements in the queue (and make all the free needed)
void CQueueClear(CalendarQueue * cqueue) {
	if (cqueue == NULL)
		perror("CQueue non initialized !");
	CQLinkedListClear(cqueue->bucket);
	return;
}

// Returns the first element of the queue
void * CQueueGet(const struct CalendarQueue * cqueue) {
	if (cqueue == NULL)
		perror("CQueue non initialized !");
	return CQLinkedListGet(cqueue->bucket);
}

// Tells if item is in the cqueue
bool CQueueIncludes(const struct CalendarQueue * cqueue, const void * item) {
	if (cqueue == NULL || cqueue->size == 0)
		return false;
	return CQLinkedListIncludes(cqueue->bucket, item);
}

// Tells if the cqueue is empty
bool CQueueIsEmpty(const struct CalendarQueue * cqueue) {
	return cqueue == NULL || CQLinkedListIsEmpty(cqueue->bucket);
}

// Adds the item in the cqueue
bool CQueuePut(struct CalendarQueue * cqueue, void * item)
{
	if (cqueue == NULL)
		perror("Putting an item in an Empty CQueue !");
	if (CQLinkedListInsert(cqueue->bucket, item)) {
		cqueue->size++;
		return true;
	}
	return false;
}

// Removes item from the cqueue if present
bool CQueueRemove(struct CalendarQueue * cqueue, const void * item) {
	if (cqueue == NULL)
		return false;
	if (CQLinkedListRemove(cqueue->bucket, item)) {
		cqueue->size--;
		return true;
	}
	return false;
}

// Gives the actual size of the cqueue
int CQueueSize(const CalendarQueue * cqueue) {
	if (cqueue == NULL)
		return 0;
	return cqueue->size;
}

// Returns the first element of the Cqueue and depops it !
void * CQueueTake(struct CalendarQueue * cqueue) {
	if (cqueue == NULL)
		return NULL;
	void* result = CQLinkedListTake(cqueue->bucket);
	if (result != NULL)
		cqueue->size--;
	return result;
}

// Deletes properly the cqueue
void CQueueDelete(struct CalendarQueue * cqueue){
	if (cqueue == NULL)
		return;
	CQLinkedListDelete(cqueue->bucket);
	free(cqueue);
	return;
}


// Functions for the CQLinkedList :

// Initialization of a list
CQLinkedList * newCQLinkedList () {
	CQLinkedList * list = NULL;
	if (list != NULL)
		CQLinkedListDelete(list);
	if ((list = malloc(sizeof(CQLinkedList))) == NULL)
		perror("Allocation Error (CQLinkedListInitialize");
	list->head = NULL;
	list->tail = NULL;
	return list;
}
// Set of a list
CQLinkedList * newCQLinkedListWithParam (CQCell* head, CQCell* tail) {
	CQLinkedList * list = NULL;
	if (list == NULL)
		if ((list = malloc(sizeof(CQLinkedList))) == NULL)
			perror("Allocation Error (CQLinkedListInitializeWithParam");
	list->head = head;
	list->tail = tail;
	return list;
}

// Search if the DEEvent item is in list
bool CQLinkedListIncludes(const CQLinkedList * list, const DEEvent * item) {
	if (CQLinkedListIsEmpty(list))
		return false;
	CQCell * p = NULL;
	for (p = list->head ; p != NULL; p = p->next) 
    if (DEEventEquals(p->content, item))
      return true;
	return false;
}

// Tells if list is empty
bool CQLinkedListIsEmpty(const CQLinkedList * list) {
	return (list == NULL || list-> head == NULL);
}

// Adds item in list
bool CQLinkedListInsert(CQLinkedList * list, DEEvent* item) {
	CQCell * newCell = NULL;

	// This should not happen
	if (list == NULL)
		return false;

	// Special case: linked list is empty.
  if (list->head == NULL) {
	  list->head = newCQCellWithParam(item, NULL);
    list->tail = list->head;
    return true;
  }

  // LinkedList is not empty.
  // I assert that by construction, when head != null,
  // then tail != null as well.
  // Special case: Check if item is smaller than or equal to tail.
  if (DEEventCompare(item, list->tail->content) >= 0) {
      // item becomes new tail.
	  newCell = newCQCellWithParam(item, NULL);
      list->tail->next = newCell;
      list->tail = newCell;
      return true;
  }

  // Check if head is strictly greater than item.
  if (DEEventCompare(list->head->content, item) > 0) {
      // object becomes the new head
	  newCell = newCQCellWithParam(item, list->head);
	  list->head = newCell;
      return true;
  }

  // No more special cases.
  // Iterate from head of queue.
  CQCell * previousCell = list->head;
  CQCell * currentCell = previousCell->next;

  // Note that this loop will always terminate via the return
  // statement. This is because tail is assured of being strictly
  // greater than object.
  do {
      // check if currentCell is strictly greater than object
      if (DEEventCompare(currentCell->content, item) > 0) {
          // insert object between previousCell and currentCell
    	  newCell = newCQCellWithParam(item, currentCell);
      	  previousCell->next = newCell;
          return true;
      }

      previousCell = currentCell;
      currentCell = previousCell->next;
  } while (currentCell != NULL);

  return false;
}

// Remove a selected item from the list
bool CQLinkedListRemove(CQLinkedList * list, const DEEvent* item) {
	if (CQLinkedListIsEmpty(list))
			return false;

	CQCell * head = list->head;
	CQCell * tail = list->tail;
	// two special cases:
  // Case 1: list is empty: always return false.
  if (head == NULL) {
      return false;
  }

  // Case 2: The element I want is at head of the list.
  if (DEEventEquals(head->content,item)) {
      if (head != tail) {
          // Linked list has at least two cells.
          head = head->next;
      } else {
          // Linked list contains only one cell
          head = NULL;
          tail = NULL;
      }
      return true;
  }

  // Non-special case that requires looping:
  CQCell * previousCell = head;
  CQCell * currentCell = previousCell->next;

  do {
      if (DEEventEquals(currentCell->content,item)) {
          // Found a match.
          previousCell->next = currentCell->next;
          return true;
      }

      previousCell = currentCell;
      currentCell = currentCell->next;
  } while (currentCell != NULL);

  // No matching entry was found.
  return false;
}

// Return the first element of the list and depop it !
struct DEEvent * CQLinkedListTake(CQLinkedList * list) {
  if (CQLinkedListIsEmpty(list))
	  return NULL;
  // remove the head
  CQCell * oldHead = list->head;
  list->head = list->head->next;

  if (list->head == NULL) {
      list->tail = NULL;
  }
  return oldHead->content;
}

// Return the first element of the list
struct DEEvent * CQLinkedListGet(CQLinkedList * list) {
	if (CQLinkedListIsEmpty(list))
		return NULL;
	return list->head->content;
}

// Clears completely (and properly) the list
void CQLinkedListClear(CQLinkedList * list) {
	if (list == NULL)
		return;

	CQCell* cellTemp;
	CQCell* oldCellTemp;
	// In case the queue is empty
	if (list->head == NULL)
		return;

	// In case the queue has only one element
	if (list->head == list->tail) {
		CQCellDelete(list->head);
		return;
	}

	// All the other cases
	for (oldCellTemp = list->head, cellTemp = list->head->next ;
			cellTemp == NULL ;
			oldCellTemp = cellTemp, cellTemp = cellTemp->next) {
		CQCellDelete(oldCellTemp);
	}
	return;
}

// Delete the list forever (a long time)
void CQLinkedListDelete(CQLinkedList * list) {
	if (list != NULL) {
		CQLinkedListClear(list);
		free(list);
	}
}
// Functions for CQCell :
CQCell * newCQCell () {
	CQCell* cell = NULL;
	if ((cell = malloc(sizeof(CQCell))) == NULL)
		perror("Allocation Error (CQCellInitialize)");

	cell->content = newDEEvent();
	cell->next = NULL;

	return cell;
}
CQCell * newCQCellWithParam (DEEvent* content, CQCell* next) {
	CQCell* cell = NULL;
	if ((cell = malloc(sizeof(CQCell))) == NULL)
		perror("Allocation Error (CQCellInitializeWithParam)");

	cell->content = content;
	cell->next = next;

	return cell;
}

// Free cell (not a joke) !
void CQCellDelete (CQCell * cell)
{
	if (cell->content != NULL)
		DEEventDelete(cell->content);
	free(cell);
}
