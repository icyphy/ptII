/* Utilities for the type infrastructure.

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

TypeData GeneralType_typeData;

/*
 * Initiate GeneralType's TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void TypeData_init(TypeData* type_data) {
	*type_data = (TypeData) {
		NULL,					// superType
		"GeneralType",			// typeName
		sizeof(GeneralType),	// size
	};
}

/**
 * Initiate a general type object.
 * 
 * @param general The general type object to be initialized.
 * @param actual_type_data The type data of the general type object's actual
 *  type, or NULL. When NULL is given (which is usually the case when called by
 *  the user), GeneralType_typeData is used.
 */
void GeneralType_init(GeneralType* general, TypeData* actual_type_data) {
	*general = (GeneralType) {
		actual_type_data == NULL ? &GeneralType_typeData : actual_type_data
		// typeData
	};
}
