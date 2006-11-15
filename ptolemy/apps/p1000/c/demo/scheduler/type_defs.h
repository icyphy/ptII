#ifndef TYPE_DEFS_H_
#define TYPE_DEFS_H_

#include <stdio.h>
#include <stdlib.h>
#include "type_uids.h"

#ifndef NULL
#define NULL 0
#endif

typedef long TYPE_UID_TYPE;

#define PUSH_BACK(last_element, new_element) \
	(last_element->next = new_element, \
	new_element->prev = last_element, \
	new_element->next = NULL, \
	last_element = new_element)

/* The general type that serves as the root of all hierarchical types. */

typedef struct GENERAL_TYPE {
	TYPE_UID_TYPE type_uid;
	void* actual_ref;
} GENERAL_TYPE;

void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref);

/* Find the reference to the super-type of the given object reference. */

#define SUPER(object_ref) \
	(object_ref->type_uid == GENERAL_TYPE_UID ? NULL : \
			(GENERAL_TYPE*) ((void*) object_ref \
			+ sizeof(object_ref->type_uid) \
			+ sizeof(object_ref->actual_ref)))

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
	SUPER_TYPE super;

#define DECLARE_OVERRIDEN_METHOD(return_type, method_name, ...) \
	return_type (*super_ ## method_name)(__VA_ARGS__);
	
#define IMPLEMENT_OVERRIDEN_METHOD(object_ref, SUPER_TYPE, method_name) \
	object_ref->super_ ## method_name = \
		((SUPER_TYPE*) SUPER(object_ref))->method_name;

#define INIT_SUPER_TYPE(TYPE, SUPER_TYPE, ref, _actual_ref, ...) do { \
		ref->type_uid = TYPE ## _UID; \
		ref->actual_ref = _actual_ref; \
		SUPER_TYPE ## _init(&(ref->super), _actual_ref , ## __VA_ARGS__); \
	} while (0)

#define COPY_METHOD_TABLE(DES_TYPE, des_object_ref, des_method_table, \
	src_method_table) \
	(memcpy(des_method_table, src_method_table, \
		sizeof(DES_TYPE) + ((void*)des_object_ref) - ((void*)des_method_table)))

/* Dependency link */

typedef struct DEPENDENCY_LINK {
	struct PORT* port;
	double dependency;
	struct DEPENDENCY_LINK* prev;
	struct DEPENDENCY_LINK* next;
} DEPENDENCY_LINK;

void DEPENDENCY_LINK_init(DEPENDENCY_LINK* link, struct PORT* port,
	double dependency);
	
/* Token */

typedef struct TOKEN {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	struct PORT* prev;
	struct PORT* next;
} TOKEN;

void TOKEN_init(TOKEN* token, void* actual_ref);

/* Int Token */

typedef struct INT_TOKEN {
	DECLARE_SUPER_TYPE(TOKEN)
	
	int value;
} INT_TOKEN;

void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref);

/* Time */

typedef struct TIME {
	unsigned int ms;
	unsigned int ns;  //FIXME: what are the fields of this struct?
} TIME;

/* Event */

typedef struct EVENT {
	TOKEN* token;
	TIME time;
	int is_timer_event : 1;
	struct EVENT* prev;
	struct EVENT* next;
} EVENT;

/* Scheduler. */

typedef struct SCHEDULER {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	int fd;
	struct ACTOR* first_actor;
	struct ACTOR* last_actor;
	struct PORT* first_port;
	struct PORT* last_port;
} SCHEDULER;

void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref);
void SCHEDULER_register_port(SCHEDULER* scheduler, struct PORT* port);
void SCHEDULER_execute(SCHEDULER* scheduler);

/* Port */

typedef struct PORT {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	struct PORT* connected_port;
	struct ACTOR* container;
	int has_token : 1;
	int is_realtime : 1;
	DEPENDENCY_LINK* last_link;
	struct PORT* prev;
	struct PORT* next;
} PORT;

void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container);
void PORT_connect(PORT* port, PORT* to_port);

/* Typed port */

typedef struct TYPED_PORT {
	DECLARE_SUPER_TYPE(PORT)
	
	EVENT* first_event;
	EVENT* last_event;
} TYPED_PORT;

void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container);
void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event);

/* Actor */

typedef struct ACTOR {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	SCHEDULER* scheduler;
	struct ACTOR* prev;
	struct ACTOR* next;
	
	/* Method table */
	// Method from ACTOR
	void (*fire)(struct ACTOR* actor);
} ACTOR;

void ACTOR_init(ACTOR* actor, void* actual_ref, SCHEDULER* scheduler,
	const void *method_table);
void ACTOR_fire(ACTOR* actor);

#endif /*TYPE_DEFS_H_*/
