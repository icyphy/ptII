/* TypedPort, a port that has a type associated with it.

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

#ifndef TYPED_PORT_H_
#define TYPED_PORT_H_

#include "types.h"
#include "port.h"
#include "event.h"
#include "bidir_list.h"

/**
 * TypedPort is a kind of ports that are typed (specifying the tokens that they
 * are allowed to receive or send). Each typed port also has an event queue for
 * the discrete event execution.
 */
typedef struct TypedPort {
	/* TypedPort is directly inherited from Port. */
	Port super;
	
	/* The event queue. */
	BidirList eventQueue;
} TypedPort;

/*
 * TypedPort's static type data.
 */
typedef struct TypedPort_TypeData {
	Port_TypeData inheritedTypeData;
	
	// send method.
	void (*send)(TypedPort* typed_port, const Event* event);
} TypedPort_TypeData;

extern TypedPort_TypeData TypedPort_typeData;

/*
 * Initiate TypedPort_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void TypedPort_TypeData_init(TypedPort_TypeData* type_data);

/* Actor is defined in actor.h. */
struct Actor;

/**
 * Initiate a typed port, and assign a container actor to it.
 * 
 * @param typed_port The typed port to be initiated.
 * @param actual_type_data The type data of the typed port's actual type, or
 *  NULL. When NULL is given (which is usually the case when called by the
 *  user), TypedPort_typeData is used.
 * @param container The actor that contains the typed port.
 */
void TypedPort_init(TypedPort* typed_port,
	TypedPort_TypeData* actual_type_data, struct Actor* container);

/**
 * Send an event via the typed port. The event will be duplicated in the heap
 * before it is sent. The given typed port must be an output port. If it is
 * connected to input port(s), then the input ports will later receive the
 * event.
 * 
 * @param typed_port The output typed port where the event will be sent.
 * @param event The event that will be duplicated in the heap and be sent.
 */
void TypedPort_send(TypedPort* typed_port, const Event* event);

#endif /*TYPED_PORT_H_*/
