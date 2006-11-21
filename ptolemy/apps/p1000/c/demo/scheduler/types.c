/* Utilities for the type infrastructure.

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

#include "types.h"

/**
 * Initiate an object of GENERAL_TYPE.
 * 
 * @param general The reference to the object of GENERAL_TYPE.
 * @param actual_ref The actual reference to the object being created, which can
 *  be of GENERAL_TYPE or its subtypes.
 */
void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref) {
	*general = (GENERAL_TYPE) {
		GENERAL_TYPE_UID,	// type_uid
		actual_ref			// actual_ref
	};
}

/**
 * Up-cast an object reference, given by object_ref, to one of its super-types.
 * type_uid is the UID of the type wanted. This type must be the same as
 * object_ref's type, or one of its super-types. The returned is the reference
 * to the same object but of the specified type. In the implementation, the
 * returned reference may not be equal to object_ref, but it is guaranteed to be
 * of the type specified by type_uid.
 * 
 * @param object_ref The reference to the object to be up-cast.
 * @param type_uid The UID of the type to be up-cast to.
 * @return The reference to the object of the specified type, or NULL if
 *  object_ref's type is not a descendant of the specified type.
 */
GENERAL_TYPE* _upcast(GENERAL_TYPE* object_ref, TYPE_UID_TYPE type_uid) {
	while (object_ref != NULL && object_ref->_type_uid != type_uid)
		object_ref = SUPER(object_ref);
	return object_ref;
}
