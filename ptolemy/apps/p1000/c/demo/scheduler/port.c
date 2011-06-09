/* Port type, the common super-type of all kinds of ports.

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

 @author Thomas Huining Feng
 
 */

#include "port.h"
#include "actor.h"

/**
 * Initiate a dependency link, and assign a dependency number.
 * 
 * @param link The dependency link to be initiated.
 * @param port The port for the dependency link.
 * @param dependency The dependency number.
 */
void DependencyLink_init(DependencyLink* link, Port* port, double dependency) {
	*link = (DependencyLink) {
		port,			// port
		dependency,		// dependency
	};
}

Port_TypeData Port_typeData;

/*
 * Initiate Port_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void Port_TypeData_init(Port_TypeData* type_data) {
	// Call the initiate method of the super-type.
	TypeData_init((TypeData*) type_data);
	
	// Override super-type.
	*((TypeData*) type_data) = (TypeData) {
		&GeneralType_typeData,		// superType
		"Port",						// typeName
		sizeof(Port),				// size
	};
	
	// Initiate data for the current type.
	type_data->connect = Port_connect;
}

/**
 * Initiate a port, and assign a container actor to it.
 * 
 * @param port The port to be initiated.
 * @param actual_type_data The type data of the port's actual type, or NULL.
 *  When NULL is given (which is usually the case when called by the user),
 *  Port_typeData is used.
 * @param container The actor that contains the port.
 */
void Port_init(Port* port, Port_TypeData* actual_type_data, Actor* container) {

	GeneralType_init((GeneralType*)port, (TypeData*)(actual_type_data == NULL ?
			&Port_typeData : actual_type_data));
	
	port->connectedPort = NULL;
	port->container = container;
	port->isRealtime = 0;
	BidirList_init(&(port->dependencyLinks));
}

/**
 * Connect a port to another port. The port argument specifies an output port,
 * and the to_port argument specifies an input port to receive the output events
 * from the output port. The connectedPort field of the output port is set with
 * the input port, but the input port's connectedPort field is not set.
 * 
 * @param port The output port.
 * @param to_port The input port.
 */
void Port_connect(Port* port, Port* to_port) {
	port->connectedPort = to_port;
}
