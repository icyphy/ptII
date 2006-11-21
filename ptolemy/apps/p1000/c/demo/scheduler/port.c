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

#include "port.h"

/**
 * Initiate an object of the DEPENDENCY_LINK type, and assign a dependency
 * number.
 * 
 * @param link Reference to the DEPENDENCY_LINK object to be initiated.
 * @param port The port for the dependency link.
 * @param dependency The dependency number.
 */
void DEPENDENCY_LINK_init(DEPENDENCY_LINK* link, struct PORT* port,
	double dependency) {
	*link = (DEPENDENCY_LINK) {
		port,		// port
		dependency,	// dependency
		NULL,		// prev
		NULL		// next
	};
}

/**
 * Initiate an object of the PORT type, and assign a container actor to it.
 * 
 * @param port Reference to the PORT object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param container Reference to the actor that contains the port.
 */
void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container) {
	INIT_SUPER_TYPE(PORT, GENERAL_TYPE, port, actual_ref, NULL);
	
	port->connected_port = NULL;
	port->container = container;
	port->is_realtime = 0;
	port->first_link = port->last_link = NULL;
	port->prev = port->next = NULL;
}

/**
 * Connect a port with another port. The port argument specifies an output port,
 * and the to_port argument specifies an input port to receive the output events
 * from the output port. The connected_port field of the output port is set with
 * the input port, but the input port's connected_port field is not set.
 * 
 * @param port The output port.
 * @param to_port The input port.
 */
void PORT_connect(PORT* port, PORT* to_port) {
	port->connected_port = to_port;
}
