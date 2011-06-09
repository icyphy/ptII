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

#ifndef TYPE_DEFS_H_
#define TYPE_DEFS_H_

#include <stdio.h>
#include <stdlib.h>

/*------------------------------*
 *       List Operations        *
 *------------------------------*/

/**
 * Define NULL if not defined
 */
#ifndef NULL
#define NULL 0
#endif

/*------------------------------*
 *     Type Infrastructure      *
 *------------------------------*/

/*
 * Struct for static type information.
 */
typedef struct TypeData {
	void* superTypeData;
	char* typeName;
	size_t size;
} TypeData;

/**
 * The general type that serves as the root of the type hierarchy.
 */
typedef struct GeneralType {
	/* Type data that are singlitons representing different types */
	TypeData* typeData;
} GeneralType;

extern TypeData GeneralType_typeData;

/*
 * Initiate GeneralType's TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void TypeData_init(TypeData* type_data);

/**
 * Initiate a general type object.
 * 
 * @param general The general type object to be initialized.
 * @param actual_type_data The type data of the general type object's actual
 *  type, or NULL. When NULL is given (which is usually the case when called by
 *  the user), GeneralType_typeData is used.
 */
void GeneralType_init(GeneralType* general, TypeData* actual_type_data);

/**
 * Invoke a virtual method belonging to the given type with the object
 * reference. The Type parameter may be object_ref's actual type or one of its
 * super-types. method_name must be the name of a virtual method belonging to
 * that type.
 * 
 * @param Type The type that object_ref belongs to. It may be object_ref's
 *  actual type, or a super-type of that actual type.
 * @param method_name The name of the virtual method to invoke.
 * @param object_ref The reference to the object.
 * @return The value returned by the method.
 */
#define INVOKE_VIRTUAL_METHOD(Type, method_name, object_ref, ...) \
	((Type ## _TypeData*)((GeneralType*)object_ref)->typeData)->method_name( \
		object_ref , ## __VA_ARGS__);

/*------------------------------*
 *      Other Definitions       *
 *------------------------------*/

/**
 * Time
 */
typedef struct Time {
	unsigned int ms;
	unsigned int ns;  //FIXME: what are the fields of this struct?
} Time;

#endif /*TYPE_DEFS_H_*/
