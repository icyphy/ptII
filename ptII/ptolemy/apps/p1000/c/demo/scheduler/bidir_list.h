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

#ifndef BIDIR_LIST_H_
#define BIDIR_LIST_H_

#include "types.h"

/**
 * Element in the bidirectional list, which allows to access its previous and
 * next element in the list, if any.
 */
typedef struct BidirListElement {
	/* The data encapsulated in the element. */
	void* payload;
	/* The previous element. */
	struct BidirListElement* prev;
	/* The next element. */
	struct BidirListElement* next;
} BidirListElement;

/**
 * Bidirectional list data structure.
 */
typedef struct BidirList {
	/* The first element in the list, or NULL if the list is empty. */
	BidirListElement* first;
	/* The last element in the list, or NULL if the list is empty. */
	BidirListElement* last;
	/* The number of elements in the list. */
	int size;
} BidirList;

/**
 * Initiate a bidirectional list.
 * 
 * @param list The bidirectional list to be initiated.
 */
void BidirList_init(BidirList* list);

/**
 * Create a new element in the heap to encapsulate the payload, and add it to
 * the end of the bidirectional list.
 * 
 * @param list The bidirectional list to be added to.
 * @param payload The payload to be encapsulated in the bidirectional list
 *  element.
 */
void BidirList_pushBack(BidirList* list, void* payload);

/**
 * Remove the first element in the bidirectional list, and return its payload.
 * 
 * @param list The bidirectional list.
 * @return The payload of the removed (first) element of the bidirectional list,
 *  or NULL if the bidirectional list is empty.
 */
void* BidirList_removeFirst(BidirList* list);

#endif /*BIDIR_LIST_H_*/
