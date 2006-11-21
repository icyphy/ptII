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

#ifndef TYPE_DEFS_H_
#define TYPE_DEFS_H_

#include <stdio.h>
#include <stdlib.h>
#include "type_uids.h"

/*------------------------------*
 *       List Operations        *
 *------------------------------*/

/**
 * Define NULL if not defined
 */
#ifndef NULL
#define NULL 0
#endif

/**
 * Append an element into a list specified by its first element and its
 * last element. Each element in the list, including the newly inserted one,
 * must have a "prev" field pointing to its previous element (if any), and also
 * a "next" field pointing to the next (if any).
 * 
 * @param first_element The first element in the list, or NULL if the list is
 *  empty.
 * @param last_element The last element in the list, or NULL if the list is
 *  empty.
 */
#define PUSH_BACK(first_element, last_element, new_element) \
	do { \
		if ((first_element) == NULL) { \
			(first_element) = (last_element) = (new_element); \
			(new_element)->prev = (new_element)->next = NULL; \
		} else { \
			(last_element)->next = (new_element); \
			(new_element)->prev = (last_element); \
			(new_element)->next = NULL; \
			(last_element) = (new_element); \
		} \
	} while (0)

/**
 * Remove the first element in a list specified by its first element and its
 * last element.
 * 
 * @param first_element The first element in the list, or NULL if the list is
 *  empty.
 * @param last_element The last element in the list, or NULL if the list is
 *  empty.
 */
#define REMOVE_FIRST(first_element, last_element) \
	do { \
		if ((first_element)->next == NULL) \
			(first_element) = (last_element) = NULL; \
		else { \
			(first_element) = (first_element)->next; \
			(first_element)->prev->next = NULL; \
			(first_element)->prev = NULL; \
		} \
	} while (0)

/**
 * Remove the last element in a list specified by its first element and its
 * last element.
 * 
 * @param first_element The first element in the list, or NULL if the list is
 *  empty.
 * @param last_element The last element in the list, or NULL if the list is
 *  empty.
 */
#define REMOVE_LAST(first_element, last_element) \
	do { \
		if ((last_element)->prev == NULL) \
			(first_element) = (last_element) = NULL; \
		else { \
			(last_element) = (last_element)->prev; \
			(last_element)->next->prev = NULL; \
			(last_element)->next = NULL; \
		} \
	} while (0)

/*------------------------------*
 *             Debug            *
 *------------------------------*/

/**
 * Test the value of expr, output err_msg to stderr if expr's value is 0, and
 * then return expr's value.
 * 
 * @param expr The expression to be tested.
 * @param err_msg The error message to be output if expr's value is 0.
 * @param return The value of the expression.
 */
#define ASSERT(expr, err_msg, ...) \
	((expr) ? (expr) : (fprintf(stderr, err_msg , ## __VA_ARGS__), (expr)))

/**
 * Test the value of expr, output err_msg and exit with exit_code if expr's
 * value is 0, and return expr's value if it is not 0.
 * 
 * @param expr The expression to be tested.
 * @param exit_code The exit code for the call to exit(int) if expr's value is
 *  not 0.
 * @param err_msg The error message to be output if expr's value is 0.
 * @return The value of the expression.
 */
#define ASSERT_FATAL(expr, exit_code, err_msg, ...) \
	((expr) ? (fprintf(stderr, err_msg , ## __VA_ARGS__), exit(exit_code)) \
			: (expr))

/*------------------------------*
 *     Type Infrastructure      *
 *------------------------------*/

/**
 * The type of type-uids. Each user-defined type is given a unique id of this
 * type.
 */
typedef long TYPE_UID_TYPE;

/**
 * The general type that serves as the root of all hierarchical types.
 */
typedef struct GENERAL_TYPE {
	/* Common fields of all types. */
	TYPE_UID_TYPE _type_uid;
	void* _actual_ref;
	void* _method_table;

	/* Fields of GENERAL_TYPE. */
	size_t size;
} GENERAL_TYPE;

/**
 * Initiate an object of GENERAL_TYPE.
 * 
 * @param general The reference to the object of GENERAL_TYPE.
 * @param actual_ref The actual reference to the object being created, which can
 *  be of GENERAL_TYPE or its subtypes.
 */
void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref);

/**
 * Convert the object reference into its super-type.
 * 
 * @param object_ref The reference to the object to be converted.
 * @return The reference to the object of the super-type.
 */
#define SUPER(object_ref) \
	(object_ref->_type_uid == GENERAL_TYPE_UID ? NULL : \
			(GENERAL_TYPE*) ((void*) object_ref \
			+ sizeof(object_ref->_type_uid) \
			+ sizeof(object_ref->_actual_ref) \
			+ sizeof(object_ref->_method_table)))

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
GENERAL_TYPE* _upcast(GENERAL_TYPE* object_ref, TYPE_UID_TYPE type_uid);

/**
 * Up-cast an object reference, given by object_ref, to one of its super-types,
 * specified by the TYPE argument.
 * 
 * @param object_ref The reference to the object to be up-cast.
 * @param TYPE The type to be up-cast to.
 * @return The reference to the object of the specified type, or NULL if
 *  object_ref's type is not a descendant of the specified type.
 * @see _upcast(GENERAL_TYPE*, TYPE_UID_TYPE)
 */
#define UPCAST(object_ref, TYPE) \
	((TYPE*) _upcast((GENERAL_TYPE*) object_ref, TYPE ## _UID))

/**
 * Cast an object reference, given by object_ref, to one of the types that it
 * actually belongs to. This may result in an up-cast or a down-cast, depending
 * upon whether the type to be cast to is a super-type of object_ref's type, or
 * a sub-type of it. In the implementation, this is achieved by up-casting the
 * actual reference of object_ref (object_ref->actual_ref).
 * 
 * @param object_ref The reference to the object to be cast.
 * @param TYPE The type to be cast to.
 * @return The reference to the object of the specified type, or NULL if
 *  object_ref's actual type is not a descendant of the specified type.
 * @see _upcast(GENERAL_TYPE*, TYPE_UID_TYPE)
 */
#define CAST(object_ref, TYPE) \
	UPCAST(((GENERAL_TYPE*) object_ref)->_actual_ref, TYPE)

/**
 * Declare the current type (modeled with a C struct) to be a sub-type of the
 * SUPER_TYPE. All the fields that the super-type has are automatically
 * inherited in the current type by this declaration. Typically, to declare type
 * T' to be a sub-type of type T, the C header file may have the following
 * declaration:
 * 
 *     typedef struct T' {
 *         DECLARE_SUPER_TYPE(T)
 *         // Here start the definitions of T''s own fields, which do not belong
 *         // to T.
 *         ...
 *     } T';
 * 
 * DECLARE_SUPER_TYPE must be the first declaration in the struct.
 * 
 * @param SUPER_TYPE The super-type of the type being defined. 
 */
#define DECLARE_SUPER_TYPE(SUPER_TYPE) \
	TYPE_UID_TYPE _type_uid; \
	void* _actual_ref; \
	void* _method_table; \
	SUPER_TYPE super;

/**
 * Call the super-type's method to initiate the part of object that belongs to
 * the super-type. It also sets the correct values of the object's pre-defined
 * fields. The object's _type_uid field is set to the UID of the given type; the
 * object's _actual_ref is set to the actual reference; the object's
 * _method_table is set to the reference to the method table for the actual
 * type. This call also sets the size field of GENERAL_TYPE, which reflects the
 * actual size allocated for the object.
 * 
 * @param TYPE The type of the reference.
 * @param SUPER_TYPE The super-type of TYPE.
 * @param ref The reference to the object.
 * @param actual_ref The actual reference to the object.
 * @param method_table The method table to use if (ref != actual_ref), meaning
 *  that the TYPE parameter is not the actual type. If (ref == actual_ref), the
 *  TYPE parameter is the actual type, so the method table for TYPE is used, and
 *  this parameter is ignored.
 */
#define INIT_SUPER_TYPE(TYPE, SUPER_TYPE, ref, actual_ref, method_table, ...) \
	do { \
		ref->_type_uid = TYPE ## _UID; \
		ref->_actual_ref = actual_ref; \
		ref->_method_table = ref == actual_ref ? \
				method_table : ((GENERAL_TYPE*) actual_ref)->_method_table; \
		SUPER_TYPE ## _init(&(ref->super), actual_ref , ## __VA_ARGS__); \
		if (ref == actual_ref) \
			UPCAST(ref, GENERAL_TYPE)->size = sizeof(TYPE); \
	} while (0)

/**
 * Invoke a virtual method belonging to the given type with the object
 * reference. The TYPE parameter may be object_ref's actual type or one of its
 * super-types. method_name must be the name of a method belonging to that type.
 * If it cannot be found as a method, or the types or the number of arguments
 * (object_ref, ...) do not match the method's declaration, a compile-time error
 * will be given. However, if the type is incorrect for object_ref, or the entry
 * for the specified method in object_ref's method table is inconsistent with
 * TYPE's method table, then a run-time error will happen, which usually lead to
 * a segmentation fault or system crash.
 * 
 * @param TYPE The type that object_ref belongs to. It may be object_ref's
 *  actual type, or a super-type of that actual type.
 * @param method_name The name of the virtual method to invoke.
 * @param object_ref The reference to the object.
 * @return The value returned by the method.
 */
#define INVOKE_VIRTUAL_METHOD(TYPE, method_name, object_ref, ...) \
	(((TYPE ## _METHOD_TABLE*)((TYPE*)object_ref)->_method_table)->method_name \
		((TYPE*)((TYPE*)object_ref)->_actual_ref , ## __VA_ARGS__ ))

/*------------------------------*
 *      Other Definitions       *
 *------------------------------*/

/**
 * Time
 */
typedef struct TIME {
	unsigned int ms;
	unsigned int ns;  //FIXME: what are the fields of this struct?
} TIME;

#endif /*TYPE_DEFS_H_*/
