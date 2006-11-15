#ifndef TOKEN_H_
#define TOKEN_H_

#include "types.h"

typedef struct TOKEN {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	struct PORT* prev;
	struct PORT* next;
} TOKEN;

void TOKEN_init(TOKEN* token, void* actual_ref);

#endif /*TOKEN_H_*/
