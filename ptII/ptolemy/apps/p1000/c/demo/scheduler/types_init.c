/* Function to initiate all the type data.

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

#include "types.h"
#include "types_init.h"

#include "actor.h"
#include "int_token.h"
#include "main.h"
#include "port.h"
#include "scheduler.h"
#include "token.h"
#include "typed_port.h"

/**
 * Initiate all the type data defined in the system.
 */
void init_type_data() {
	TypeData_init(&GeneralType_typeData);
	
	Actor_TypeData_init(&Actor_typeData);
	Clock_TypeData_init(&Clock_typeData);
	IntToken_TypeData_init(&IntToken_typeData);
	Port_TypeData_init(&Port_typeData);
	Scheduler_TypeData_init(&Scheduler_typeData);
	Token_TypeData_init(&Token_typeData);
	TriggeredClock_TypeData_init(&TriggeredClock_typeData);
	TriggerOut_TypeData_init(&TriggerOut_typeData);
	TypedPort_TypeData_init(&TypedPort_typeData);
}
