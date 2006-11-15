#ifndef TYPE_DEFS_H_
#define TYPE_DEFS_H_

#include <stdio.h>
#include <stdlib.h>
#include "type_uids.h"

#ifndef NULL
#define NULL 0
#endif

typedef long TYPE_UID_TYPE;

#define PUSH_BACK(first_element, last_element, new_element) do { \
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

#define REMOVE_FIRST(first_element, last_element) do { \
		if ((first_element)->next == NULL) \
			(first_element) = (last_element) = NULL; \
		else { \
			(first_element) = (first_element)->next; \
			(first_element)->prev->next = NULL; \
			(first_element)->prev = NULL; \
		} \
	} while (0)

#define REMOVE_LAST(first_element, last_element) do { \
		if ((last_element)->prev == NULL) \
			(first_element) = (last_element) = NULL; \
		else { \
			(last_element) = (last_element)->prev; \
			(last_element)->next->prev = NULL; \
			(last_element)->next = NULL; \
		} \
	} while (0)

#define ASSERT(expr, err_msg, ...) \
	((expr) ? (fprintf(stderr, err_msg , ## __VA_ARGS__), (expr)) : (expr))

#define ASSERT_FATAL(expr, exit_code, err_msg, ...) \
	((expr) ? (fprintf(stderr, err_msg , ## __VA_ARGS__), exit(exit_code)) \
			: (expr))

/* The general type that serves as the root of all hierarchical types. */

typedef struct GENERAL_TYPE {
	TYPE_UID_TYPE type_uid;
	void* actual_ref;
	void* method_table;
} GENERAL_TYPE;

void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref);

/* Find the reference to the super-type of the given object reference. */

#define SUPER(object_ref) \
	(object_ref->type_uid == GENERAL_TYPE_UID ? NULL : \
			(GENERAL_TYPE*) ((void*) object_ref \
			+ sizeof(object_ref->type_uid) \
			+ sizeof(object_ref->actual_ref) \
			+ sizeof(object_ref->method_table)))

/* Test whether an object is a descendant of the given type (identified by its
 * type uid). */
 
GENERAL_TYPE* _upcast(GENERAL_TYPE* object_ref, TYPE_UID_TYPE type_uid);

#define UPCAST(object_ref, TYPE) \
	((TYPE*) _upcast((GENERAL_TYPE*) object_ref, TYPE ## _UID))

#define CAST(object_ref, TYPE) \
	((TYPE*) _upcast((GENERAL_TYPE*) ((GENERAL_TYPE*) object_ref)->actual_ref, \
		TYPE ## _UID))
	
#define DECLARE_SUPER_TYPE(SUPER_TYPE) \
	TYPE_UID_TYPE type_uid; \
	void* actual_ref; \
	void* method_table; \
	SUPER_TYPE super;

#define INIT_SUPER_TYPE(TYPE, SUPER_TYPE, ref, _actual_ref, _method_table, \
	...) do { \
		ref->type_uid = TYPE ## _UID; \
		ref->actual_ref = _actual_ref; \
		ref->method_table = ref == _actual_ref ? \
				_method_table : ((GENERAL_TYPE*) _actual_ref)->method_table; \
		SUPER_TYPE ## _init(&(ref->super), _actual_ref , ## __VA_ARGS__); \
	} while (0)

#define INVOKE_VIRTUAL_METHOD(TYPE, method_name, object_ref, ...) \
	(((TYPE ## _METHOD_TABLE*)((TYPE*)object_ref)->method_table)->method_name( \
		(TYPE*)((TYPE*)object_ref)->actual_ref , ## __VA_ARGS__ ))

/* Dependency link */

/* Time */

typedef struct TIME {
	unsigned int ms;
	unsigned int ns;  //FIXME: what are the fields of this struct?
} TIME;

#endif /*TYPE_DEFS_H_*/
