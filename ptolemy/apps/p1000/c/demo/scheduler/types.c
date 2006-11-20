#include "types.h"

void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref)
{
	*general = (GENERAL_TYPE) {
		GENERAL_TYPE_UID,	// type_uid
		actual_ref			// actual_ref
	};
}

GENERAL_TYPE* _upcast(GENERAL_TYPE* object_ref,
	TYPE_UID_TYPE type_uid)
{
	while (object_ref != NULL && object_ref->_type_uid != type_uid)
		object_ref = SUPER(object_ref);
	return object_ref;
}
