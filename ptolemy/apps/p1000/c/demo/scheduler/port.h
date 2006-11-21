/* PORT type, the general super-type of all the port types.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 @author Thomas Huining Feng
 
 */

#ifndef PORT_H_
#define PORT_H_

#include "types.h"

/**
 * Dependency link for the ports.
 */
typedef struct DEPENDENCY_LINK {
	/* The port. */
	struct PORT* port;
	/* The dependency. */
	double dependency;
	/* The previous dependency link in a list. */
	struct DEPENDENCY_LINK* prev;
	/* The next dependency link in a list. */
	struct DEPENDENCY_LINK* next;
} DEPENDENCY_LINK;

/**
 * Initiate an object of the DEPENDENCY_LINK type, and assign a dependency
 * number.
 * 
 * @param link Reference to the DEPENDENCY_LINK object to be initiated.
 * @param port The port for the dependency link.
 * @param dependency The dependency number.
 */
void DEPENDENCY_LINK_init(DEPENDENCY_LINK* link, struct PORT* port,
	double dependency);

/**
 * PORT type, the general super-type of all the port types.
 */
typedef struct PORT {
	/* PORT type is directly inherited from GENERAL_TYPE. */
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	/* The port that this port is connected to (null if none). */
	struct PORT* connected_port;
	/* The actor that contains this port. */
	struct ACTOR* container;
	/* Whether the port is a realtime port. */
	int is_realtime : 1;
	/* The first dependency link in the dependency link list. */
	DEPENDENCY_LINK* first_link;
	/* The last dependency link in the dependency link list. */
	DEPENDENCY_LINK* last_link;
	/* The previous port in a port list. */
	struct PORT* prev;
	/* The next port in a port list. */
	struct PORT* next;
} PORT;

/**
 * Initiate an object of the PORT type, and assign a container actor to it.
 * 
 * @param port Reference to the PORT object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param container Reference to the actor that contains the port.
 */
void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container);

/**
 * Connect a port with another port. The port argument specifies an output port,
 * and the to_port argument specifies an input port to receive the output events
 * from the output port. The connected_port field of the output port is set with
 * the input port, but the input port's connected_port field is not set.
 * 
 * @param port The output port.
 * @param to_port The input port.
 */
void PORT_connect(PORT* port, PORT* to_port);

#endif /*PORT_H_*/
