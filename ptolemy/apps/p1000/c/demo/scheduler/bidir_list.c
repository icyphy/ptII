/* Bidirectional list data structure.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Thomas Huining Feng, Yang Zhao
 
 */

#include "bidir_list.h"

/**
 * Initiate a bidirectional list.
 * 
 * @param list The bidirectional list to be initiated.
 */
void BidirList_init(BidirList* list) {
	*list = (BidirList) {
		NULL,	// first
		NULL,	// last
		0		// size
	};
}

/**
 * Create a new element in the heap to encapsulate the payload, and add it to
 * the end of the bidirectional list.
 * 
 * @param list The bidirectional list to be added to.
 * @param payload The payload to be encapsulated in the bidirectional list
 *  element.
 */
void BidirList_pushBack(BidirList* list, void* payload) {
	BidirListElement* element =
		(BidirListElement*) malloc(sizeof(BidirListElement));
	*element = (BidirListElement) {
		payload,	// payload
		NULL,		// prev
		NULL		// next
	};
	if (list->last == NULL) {
		list->first = list->last = element;
	} else {
		list->last->next = element;
		element->prev = list->last;
		list->last = element;
	}
	list->size++;
}

/**
 * Remove the first element in the bidirectional list, and return its payload.
 * 
 * @param list The bidirectional list.
 * @return The payload of the removed (first) element of the bidirectional list,
 *  or NULL if the bidirectional list is empty.
 */
void* BidirList_removeFirst(BidirList* list) {
	BidirListElement* first;
	void* payload;
	if (list->first == NULL) {
		return NULL;
	} else {
		first = list->first;
		payload = first->payload;
		if (first->next == NULL) {
			list->first = list->last = NULL;
		} else {
			first->next->prev = NULL;
			list->first = first->next;
		}
		list->size--;
		free(first);
		return payload;
	}
}
