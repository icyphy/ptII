#ifndef INT_TOKEN_H_
#define INT_TOKEN_H_

#include "types.h"
#include "token.h"

typedef struct INT_TOKEN {
	DECLARE_SUPER_TYPE(TOKEN)
	
	int value;
} INT_TOKEN;

void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref);

#endif /*INT_TOKEN_H_*/
